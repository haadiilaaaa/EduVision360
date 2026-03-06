package lk.icbt.eduvision.eduvision360.prediction.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.EmailService;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lk.icbt.eduvision.eduvision360.prediction.client.DropoutPredictionClient;
import lk.icbt.eduvision.eduvision360.prediction.dto.*;
import lk.icbt.eduvision.eduvision360.prediction.model.DropoutPrediction;
import lk.icbt.eduvision.eduvision360.prediction.model.StudentFeatureSnapshot;
import lk.icbt.eduvision.eduvision360.prediction.repository.DropoutPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DropoutPredictionServiceImpl implements DropoutPredictionService {

    private final DropoutPredictionClient dropoutPredictionClient;
    private final DropoutPredictionRepository dropoutPredictionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentFeatureSnapshotService studentFeatureSnapshotService;

    // ✅ add these
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public DropoutPredictionResponse predict(DropoutPredictionRequest request) {
        return dropoutPredictionClient.predict(request);
    }

    @Override
    public DropoutPredictionResponse runAndSave(DropoutPredictionRunRequest request, Authentication authentication) {
        String authUserId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("UNKNOWN");

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if ("TEACHER".equals(role)) {
            if (course.getTeacherId() == null || !course.getTeacherId().equals(authUserId)) {
                throw new AccessDeniedException("You can only run predictions for your own courses");
            }
        }

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId());
        if (!enrolled) {
            throw new IllegalArgumentException("Selected student is not enrolled in the selected course");
        }

        User teacher = null;
        if (course.getTeacherId() != null) {
            teacher = userRepository.findById(course.getTeacherId()).orElse(null);
        }

        Map<String, Object> featureOverrides =
                request.getFeatureOverrides() == null ? Map.of() : request.getFeatureOverrides();

        int windowDays = (request.getWindowDays() == null || request.getWindowDays() <= 0)
                ? 30
                : request.getWindowDays();

        StudentFeatureSnapshot snapshot = studentFeatureSnapshotService.buildAndSaveSnapshot(
                student.getId(),
                course.getId(),
                windowDays,
                featureOverrides
        );

        DropoutPredictionRequest fastApiRequest = new DropoutPredictionRequest(
                student.getId(),
                student.getFullName(),
                course.getId(),
                snapshot.getFinalFeatures()
        );

        DropoutPredictionResponse fastApiResponse = dropoutPredictionClient.predict(fastApiRequest);

        DropoutPrediction prediction = dropoutPredictionRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseGet(DropoutPrediction::new);

        // ✅ keep old values (anti-spam logic)
        String oldRisk = prediction.getRiskLevel();
        Instant oldPredictedAt = prediction.getPredictedAt();

        prediction.setStudentId(student.getId());
        prediction.setStudentName(student.getFullName());
        prediction.setStudentEmail(student.getEmail());

        prediction.setCourseId(course.getId());
        prediction.setCourseCode(course.getCourseCode());
        prediction.setCourseTitle(course.getTitle());

        prediction.setTeacherId(course.getTeacherId());
        prediction.setTeacherName(teacher != null ? teacher.getFullName() : null);

        prediction.setDropoutProbability(fastApiResponse.getDropoutProbability());
        prediction.setPredictedLabel(fastApiResponse.getPredictedLabel());
        prediction.setRiskLevel(fastApiResponse.getRiskLevel());
        prediction.setThreshold(fastApiResponse.getThreshold());

        prediction.setPredictedByUserId(authUserId);
        prediction.setPredictedByRole(role);
        prediction.setSnapshotId(snapshot.getId());
        prediction.setWindowDays(windowDays);
        prediction.setPredictedAt(Instant.now());
        prediction.setFeatureOverrides(featureOverrides);

        DropoutPrediction saved = dropoutPredictionRepository.save(prediction);

        // ✅ trigger notifications + emails
        postPredictionAlerts(saved, student, teacher, authUserId, role, oldRisk, oldPredictedAt);

        return toResponse(saved);
    }

    private void postPredictionAlerts(
            DropoutPrediction saved,
            User student,
            User teacher,
            String predictedByUserId,
            String predictedByRole,
            String oldRisk,
            Instant oldPredictedAt
    ) {
        String risk = saved.getRiskLevel() == null ? "" : saved.getRiskLevel().toUpperCase();

        boolean riskChanged = oldRisk == null || !oldRisk.equalsIgnoreCase(risk);

        // Notifications cooldown (12h)
        boolean notifyStale = oldPredictedAt == null ||
                oldPredictedAt.isBefore(Instant.now().minus(Duration.ofHours(12)));
        boolean shouldNotify = riskChanged || notifyStale;

        // Email cooldown (6h) - separate so HIGH email is not blocked
        boolean emailStale = oldPredictedAt == null ||
                oldPredictedAt.isBefore(Instant.now().minus(Duration.ofHours(6)));
        boolean shouldEmail = riskChanged || emailStale;

        String relatedId = saved.getId();
        String relatedType = "DROPOUT_PREDICTION";

        // ---------------- STUDENT notifications ----------------
        if ("HIGH".equals(risk)) {
            if (shouldNotify) {
                notificationService.createNotificationWithCooldown(
                        student.getId(),
                        "Support recommended",
                        "Your learning analytics suggests you may benefit from extra academic support. Please review your progress dashboard and reach out if needed.",
                        NotificationType.DROPOUT_HIGH_RISK,
                        relatedId,
                        relatedType,
                        "/student", // ✅ you don't have /student/dashboard route in App.jsx
                        Duration.ofHours(12)
                );
            }
        } else if ("MEDIUM".equals(risk)) {
            if (shouldNotify) {
                notificationService.createNotificationWithCooldown(
                        student.getId(),
                        "Progress check suggested",
                        "Your learning analytics suggests you should review your learning progress and stay consistent this week.",
                        NotificationType.DROPOUT_MEDIUM_RISK,
                        relatedId,
                        relatedType,
                        "/student",
                        Duration.ofHours(12)
                );
            }
        } else {
            // LOW -> do nothing
            return;
        }

        // ---------------- TEACHER notification + HIGH email ----------------
        if (saved.getTeacherId() != null && !saved.getTeacherId().isBlank()) {

            // If teacher object was null, fetch it safely
            if (teacher == null) {
                teacher = userRepository.findById(saved.getTeacherId()).orElse(null);
            }

            if (shouldNotify) {
                NotificationType teacherType = "HIGH".equals(risk)
                        ? NotificationType.DROPOUT_HIGH_RISK
                        : NotificationType.DROPOUT_MEDIUM_RISK;

                notificationService.createNotificationWithCooldown(
                        saved.getTeacherId(),
                        "Dropout risk alert (" + saved.getRiskLevel() + ")",
                        "Student " + saved.getStudentName() + " was flagged as " + saved.getRiskLevel()
                                + " risk in " + saved.getCourseCode() + ".",
                        teacherType,
                        relatedId,
                        relatedType,
                        "/teacher", // ✅ you don't have /teacher/predictions route in App.jsx
                        Duration.ofHours(12)
                );
            }

            // ✅ Email only for HIGH + separate email cooldown
            if ("HIGH".equals(risk) && shouldEmail
                    && teacher != null
                    && teacher.getEmail() != null
                    && !teacher.getEmail().isBlank()) {

                String teacherMsg = "Student: " + saved.getStudentName() + " (" + saved.getStudentEmail() + ")\n"
                        + "Course: " + saved.getCourseCode() + " - " + saved.getCourseTitle() + "\n"
                        + "Risk: " + saved.getRiskLevel() + "\n"
                        + "Probability: " + saved.getDropoutProbability() + "\n"
                        + "Predicted at: " + saved.getPredictedAt();

                emailService.sendEmail(
                        teacher.getEmail(),
                        "EduVision360 Alert: High Dropout Risk (" + saved.getCourseCode() + ")",
                        "Hello " + (teacher.getFullName() != null ? teacher.getFullName() : "") + ",\n\n"
                                + teacherMsg
                                + "\n\nRegards,\nEduVision360",
                        "DROPOUT_HIGH_RISK",
                        relatedId,
                        relatedType
                );
            }
        }

        // ---------------- ADMIN WHO RAN IT (optional) ----------------
        if ("ADMIN".equalsIgnoreCase(predictedByRole) && shouldNotify) {

            notificationService.createNotificationWithCooldown(
                    predictedByUserId,
                    "Prediction completed (" + saved.getRiskLevel() + ")",
                    "Dropout prediction completed for " + saved.getStudentName()
                            + " in " + saved.getCourseCode() + ". Risk: " + saved.getRiskLevel() + ".",
                    "HIGH".equals(risk) ? NotificationType.DROPOUT_HIGH_RISK : NotificationType.DROPOUT_MEDIUM_RISK,
                    relatedId,
                    relatedType,
                    "/admin",
                    Duration.ofHours(6)
            );

            // Optional: email admin only if HIGH
            if ("HIGH".equals(risk) && shouldEmail) {
                User adminUser = userRepository.findById(predictedByUserId).orElse(null);
                if (adminUser != null && adminUser.getEmail() != null && !adminUser.getEmail().isBlank()) {
                    emailService.sendEmail(
                            adminUser.getEmail(),
                            "EduVision360 Admin Alert: High Dropout Risk (" + saved.getCourseCode() + ")",
                            "Hello,\n\nA high-risk dropout prediction was generated.\n\n"
                                    + "Student: " + saved.getStudentName() + " (" + saved.getStudentEmail() + ")\n"
                                    + "Course: " + saved.getCourseCode() + " - " + saved.getCourseTitle() + "\n"
                                    + "Probability: " + saved.getDropoutProbability() + "\n"
                                    + "Predicted at: " + saved.getPredictedAt()
                                    + "\n\nRegards,\nEduVision360",
                            "DROPOUT_HIGH_RISK",
                            relatedId,
                            relatedType
                    );
                }
            }
        }
    }

    @Override
    public List<PredictionStudentOptionResponse> getStudentsForCourse(String courseId, Authentication authentication) {
        String authUserId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("UNKNOWN");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if ("TEACHER".equals(role)) {
            if (course.getTeacherId() == null || !course.getTeacherId().equals(authUserId)) {
                throw new AccessDeniedException("You can only access students for your own courses");
            }
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdOrderByEnrolledAtDesc(courseId);

        return enrollments.stream().map(e -> {
            User student = userRepository.findById(e.getStudentId()).orElse(null);
            if (student == null) return null;

            return new PredictionStudentOptionResponse(
                    student.getId(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getStudentCode()
            );
        }).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    public PredictionSummaryResponse getTeacherSummary(Authentication authentication) {
        String teacherId = authentication.getName();

        long high = dropoutPredictionRepository.countByTeacherIdAndRiskLevel(teacherId, "HIGH");
        long medium = dropoutPredictionRepository.countByTeacherIdAndRiskLevel(teacherId, "MEDIUM");
        long low = dropoutPredictionRepository.countByTeacherIdAndRiskLevel(teacherId, "LOW");

        List<DropoutPredictionResponse> recent = dropoutPredictionRepository
                .findTop10ByTeacherIdOrderByPredictedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();

        return new PredictionSummaryResponse(
                high + medium + low,
                high,
                medium,
                low,
                recent
        );
    }

    @Override
    public PredictionSummaryResponse getAdminSummary() {
        long high = dropoutPredictionRepository.countByRiskLevel("HIGH");
        long medium = dropoutPredictionRepository.countByRiskLevel("MEDIUM");
        long low = dropoutPredictionRepository.countByRiskLevel("LOW");

        List<DropoutPredictionResponse> recent = dropoutPredictionRepository
                .findTop10ByOrderByPredictedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PredictionSummaryResponse(
                high + medium + low,
                high,
                medium,
                low,
                recent
        );
    }

    private DropoutPredictionResponse toResponse(DropoutPrediction p) {
        return new DropoutPredictionResponse(
                p.getStudentId(),
                p.getStudentName(),
                p.getStudentEmail(),
                p.getCourseId(),
                p.getCourseCode(),
                p.getCourseTitle(),
                p.getTeacherId(),
                p.getTeacherName(),
                p.getDropoutProbability(),
                p.getPredictedLabel(),
                p.getRiskLevel(),
                p.getThreshold(),
                p.getPredictedAt()
        );
    }

    @Override
    public List<DropoutPredictionResponse> getTeacherPredictionsForCourse(String courseId, Authentication authentication) {
        String teacherId = authentication.getName();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("You can only access predictions for your own courses");
        }

        return dropoutPredictionRepository
                .findByTeacherIdAndCourseIdOrderByPredictedAtDesc(teacherId, courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<DropoutPredictionResponse> getAllPredictionsForAdmin() {
        return dropoutPredictionRepository.findAllByOrderByPredictedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DropoutPredictionResponse getMyLatestPrediction(Authentication authentication) {
        String studentId = authentication.getName();

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        DropoutPrediction prediction = dropoutPredictionRepository
                .findTopByStudentEmailOrderByPredictedAtDesc(student.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No prediction found for this student"));

        return toResponse(prediction);
    }

    @Override
    public List<DropoutPredictionResponse> getMyAllPredictions(Authentication authentication) {
        String studentId = authentication.getName();

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return dropoutPredictionRepository.findByStudentEmailOrderByPredictedAtDesc(student.getEmail())
                .stream()
                .map(this::toResponse)
                .toList();
    }
}