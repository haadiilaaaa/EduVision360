package lk.icbt.eduvision.eduvision360.progress.service;

import lk.icbt.eduvision.eduvision360.ai.repository.AiInteractionRepository;
import lk.icbt.eduvision.eduvision360.attendance.model.Attendance;
import lk.icbt.eduvision.eduvision360.attendance.repository.AttendanceRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.engagement.model.EngagementLog;
import lk.icbt.eduvision.eduvision360.engagement.repository.EngagementLogRepository;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.material.repository.MaterialViewLogRepository;
import lk.icbt.eduvision.eduvision360.prediction.model.DropoutPrediction;
import lk.icbt.eduvision.eduvision360.prediction.model.StudentFeatureSnapshot;
import lk.icbt.eduvision.eduvision360.prediction.repository.DropoutPredictionRepository;
import lk.icbt.eduvision.eduvision360.prediction.service.StudentFeatureSnapshotService;
import lk.icbt.eduvision.eduvision360.progress.dto.ProgressAnalyticsResponse;
import lk.icbt.eduvision.eduvision360.progress.dto.ProgressAnalyticsRunRequest;
import lk.icbt.eduvision.eduvision360.progress.model.StudentProgressAnalytics;
import lk.icbt.eduvision.eduvision360.progress.repository.StudentProgressAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentProgressAnalyticsServiceImpl implements StudentProgressAnalyticsService {

    private final StudentProgressAnalyticsRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentFeatureSnapshotService studentFeatureSnapshotService;
    private final EngagementLogRepository engagementLogRepository;
    private final AttendanceRepository attendanceRepository;
    private final MaterialViewLogRepository materialViewLogRepository;
    private final AiInteractionRepository aiInteractionRepository;
    private final DropoutPredictionRepository dropoutPredictionRepository;

    @Override
    public ProgressAnalyticsResponse runAndSave(ProgressAnalyticsRunRequest request, Authentication authentication) {
        if (request == null || request.getStudentId() == null || request.getStudentId().isBlank()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (request.getCourseId() == null || request.getCourseId().isBlank()) {
            throw new IllegalArgumentException("Course ID is required");
        }

        String authUserId = authentication.getName();
        String role = resolveRole(authentication);

        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            throw new AccessDeniedException("Only admin or teacher can run progress analytics");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if ("TEACHER".equals(role)) {
            if (course.getTeacherId() == null || !course.getTeacherId().equals(authUserId)) {
                throw new AccessDeniedException("You can only run progress analytics for your own courses");
            }
        }

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId());
        if (!enrolled) {
            throw new IllegalArgumentException("Selected student is not enrolled in the selected course");
        }

        int windowDays = (request.getWindowDays() == null || request.getWindowDays() <= 0)
                ? 30
                : request.getWindowDays();

        StudentFeatureSnapshot snapshot = studentFeatureSnapshotService.buildAndSaveSnapshot(
                student.getId(),
                course.getId(),
                windowDays,
                Map.of()
        );

        Map<String, Object> finalFeatures = snapshot.getFinalFeatures() == null
                ? Map.of()
                : snapshot.getFinalFeatures();

        long attendancePresentCountWindow = asLong(finalFeatures.get("attendance_present_count_window"));
        long materialViewsCountWindow = asLong(finalFeatures.get("material_views_count_window"));
        long aiTotalCountWindow = asLong(finalFeatures.get("ai_total_count_window"));
        long daysSinceLastLogin = asLong(finalFeatures.get("days_since_last_login"));
        long daysSinceLastAttendance = asLong(finalFeatures.get("days_since_last_attendance"));
        long daysSinceLastMaterialView = asLong(finalFeatures.get("days_since_last_material_view"));
        long daysSinceLastAiUse = asLong(finalFeatures.get("days_since_last_ai_use"));

        EngagementWindowMetrics engagementMetrics = buildEngagementMetrics(
                student.getId(),
                course.getId(),
                windowDays
        );

        DropoutPrediction latestPrediction = dropoutPredictionRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElse(null);

        double progressScore = calculateProgressScore(
                attendancePresentCountWindow,
                materialViewsCountWindow,
                aiTotalCountWindow,
                daysSinceLastLogin,
                daysSinceLastAttendance,
                daysSinceLastMaterialView,
                engagementMetrics.averageEngagementScore(),
                latestPrediction
        );

        String trend = calculateTrend(student.getId(), course.getId());
        String status = calculateStatus(progressScore, latestPrediction);
        List<String> reasons = buildReasons(
                attendancePresentCountWindow,
                materialViewsCountWindow,
                aiTotalCountWindow,
                daysSinceLastLogin,
                daysSinceLastAttendance,
                daysSinceLastMaterialView,
                daysSinceLastAiUse,
                engagementMetrics,
                latestPrediction,
                trend
        );
        String interventionSuggestion = buildInterventionSuggestion(
                attendancePresentCountWindow,
                materialViewsCountWindow,
                aiTotalCountWindow,
                daysSinceLastLogin,
                daysSinceLastAttendance,
                daysSinceLastMaterialView,
                engagementMetrics.averageEngagementScore(),
                latestPrediction,
                progressScore
        );

        StudentProgressAnalytics analytics = progressRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseGet(StudentProgressAnalytics::new);

        analytics.setStudentId(student.getId());
        analytics.setStudentName(student.getFullName());
        analytics.setStudentEmail(student.getEmail());

        analytics.setCourseId(course.getId());
        analytics.setCourseCode(course.getCourseCode());
        analytics.setCourseTitle(course.getTitle());

        analytics.setWindowDays(windowDays);

        analytics.setProgressScore(progressScore);
        analytics.setTrend(trend);
        analytics.setStatus(status);
        analytics.setInterventionSuggestion(interventionSuggestion);

        analytics.setDropoutRiskLevel(latestPrediction != null ? latestPrediction.getRiskLevel() : null);
        analytics.setDropoutProbability(latestPrediction != null ? latestPrediction.getDropoutProbability() : null);

        analytics.setAttendancePresentCountWindow(attendancePresentCountWindow);
        analytics.setMaterialViewsCountWindow(materialViewsCountWindow);
        analytics.setAiTotalCountWindow(aiTotalCountWindow);
        analytics.setDaysSinceLastLogin(daysSinceLastLogin);
        analytics.setDaysSinceLastAttendance(daysSinceLastAttendance);
        analytics.setDaysSinceLastMaterialView(daysSinceLastMaterialView);
        analytics.setDaysSinceLastAiUse(daysSinceLastAiUse);

        analytics.setAverageEngagementScore(engagementMetrics.averageEngagementScore());
        analytics.setAttentiveCount(engagementMetrics.attentiveCount());
        analytics.setNeutralCount(engagementMetrics.neutralCount());
        analytics.setDistractedCount(engagementMetrics.distractedCount());

        analytics.setSnapshotId(snapshot.getId());
        analytics.setReasons(reasons);
        analytics.setGeneratedAt(Instant.now());

        StudentProgressAnalytics saved = progressRepository.save(analytics);
        return toResponse(saved);
    }

    @Override
    public List<ProgressAnalyticsResponse> getMyProgress(Authentication authentication) {
        String studentId = authentication.getName();

        return progressRepository.findByStudentIdOrderByGeneratedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProgressAnalyticsResponse> getTeacherCourseProgress(String courseId, Authentication authentication) {
        String teacherId = authentication.getName();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("You can only access progress analytics for your own courses");
        }

        return progressRepository.findByCourseIdOrderByGeneratedAtDesc(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProgressAnalyticsResponse> getAllProgressForAdmin() {
        return progressRepository.findAllByOrderByGeneratedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EngagementWindowMetrics buildEngagementMetrics(String studentId, String courseId, int windowDays) {
        Instant cutoff = Instant.now().minus(windowDays, ChronoUnit.DAYS);

        List<EngagementLog> logs = engagementLogRepository
                .findByStudentIdAndCourseIdAndCapturedAtAfterOrderByCapturedAtDesc(studentId, courseId, cutoff);

        long attentiveCount = logs.stream()
                .filter(log -> "ATTENTIVE".equalsIgnoreCase(log.getLabel()))
                .count();

        long neutralCount = logs.stream()
                .filter(log -> "NEUTRAL".equalsIgnoreCase(log.getLabel()))
                .count();

        long distractedCount = logs.stream()
                .filter(log -> "DISTRACTED".equalsIgnoreCase(log.getLabel()))
                .count();

        Double averageEngagementScore = logs.stream()
                .map(EngagementLog::getEngagementScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);

        return new EngagementWindowMetrics(
                averageEngagementScore,
                attentiveCount,
                neutralCount,
                distractedCount
        );
    }

    private double calculateProgressScore(
            long attendancePresentCountWindow,
            long materialViewsCountWindow,
            long aiTotalCountWindow,
            long daysSinceLastLogin,
            long daysSinceLastAttendance,
            long daysSinceLastMaterialView,
            Double averageEngagementScore,
            DropoutPrediction latestPrediction
    ) {
        double attendanceComponent = calculateAttendanceComponent(
                attendancePresentCountWindow,
                daysSinceLastAttendance
        );

        double materialComponent = calculateMaterialComponent(
                materialViewsCountWindow,
                daysSinceLastMaterialView
        );

        double aiComponent = calculateAiComponent(aiTotalCountWindow);

        double loginComponent = calculateLoginComponent(daysSinceLastLogin);

        double engagementComponent = calculateEngagementComponent(averageEngagementScore);

        double dropoutContextComponent = calculateDropoutContextComponent(latestPrediction);

        return round2(
                attendanceComponent
                        + materialComponent
                        + aiComponent
                        + loginComponent
                        + engagementComponent
                        + dropoutContextComponent
        );
    }

    private double calculateAttendanceComponent(long count, long daysSinceLastAttendance) {
        double score;
        if (count >= 4) {
            score = 25.0;
        } else if (count >= 2) {
            score = 18.0;
        } else if (count == 1) {
            score = 10.0;
        } else {
            score = 4.0;
        }

        if (daysSinceLastAttendance > 14) {
            score -= 6.0;
        } else if (daysSinceLastAttendance > 7) {
            score -= 3.0;
        }

        return clamp(score, 0.0, 25.0);
    }

    private double calculateMaterialComponent(long count, long daysSinceLastMaterialView) {
        double score;
        if (count >= 6) {
            score = 20.0;
        } else if (count >= 3) {
            score = 14.0;
        } else if (count >= 1) {
            score = 8.0;
        } else {
            score = 2.0;
        }

        if (daysSinceLastMaterialView > 14) {
            score -= 4.0;
        } else if (daysSinceLastMaterialView > 7) {
            score -= 2.0;
        }

        return clamp(score, 0.0, 20.0);
    }

    private double calculateAiComponent(long count) {
        if (count >= 5) {
            return 15.0;
        }
        if (count >= 2) {
            return 10.0;
        }
        if (count == 1) {
            return 6.0;
        }
        return 2.0;
    }

    private double calculateLoginComponent(long daysSinceLastLogin) {
        if (daysSinceLastLogin <= 3) {
            return 10.0;
        }
        if (daysSinceLastLogin <= 7) {
            return 8.0;
        }
        if (daysSinceLastLogin <= 14) {
            return 5.0;
        }
        return 2.0;
    }

    private double calculateEngagementComponent(Double averageEngagementScore) {
        if (averageEngagementScore == null) {
            return 10.0; // neutral default when no engagement logs exist
        }
        if (averageEngagementScore >= 0.75) {
            return 20.0;
        }
        if (averageEngagementScore >= 0.55) {
            return 14.0;
        }
        if (averageEngagementScore >= 0.35) {
            return 8.0;
        }
        return 4.0;
    }

    private double calculateDropoutContextComponent(DropoutPrediction latestPrediction) {
        if (latestPrediction == null || latestPrediction.getRiskLevel() == null) {
            return 6.0;
        }

        String risk = latestPrediction.getRiskLevel().trim().toUpperCase();
        return switch (risk) {
            case "LOW" -> 10.0;
            case "MEDIUM" -> 6.0;
            case "HIGH" -> 2.0;
            default -> 6.0;
        };
    }

    private String calculateTrend(String studentId, String courseId) {
        Instant now = Instant.now();
        Instant recentCutoff = now.minus(7, ChronoUnit.DAYS);
        Instant previousCutoff = now.minus(14, ChronoUnit.DAYS);

        LocalDate today = LocalDate.now();
        LocalDate recentStart = today.minusDays(6);    // last 7 days including today
        LocalDate previousStart = today.minusDays(13); // previous 7 days

        List<Attendance> attendance = attendanceRepository.findByStudentIdAndCourseIdOrderByDateDescTimeDesc(studentId, courseId);

        long attendanceRecent = attendance.stream()
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(recentStart))
                .count();

        long attendancePrevious = attendance.stream()
                .filter(a -> a.getDate() != null
                        && !a.getDate().isBefore(previousStart)
                        && a.getDate().isBefore(recentStart))
                .count();

        long materialRecent = materialViewLogRepository.countByStudentIdAndCourseIdAndViewedAtAfter(
                studentId,
                courseId,
                recentCutoff
        );
        long materialLast14 = materialViewLogRepository.countByStudentIdAndCourseIdAndViewedAtAfter(
                studentId,
                courseId,
                previousCutoff
        );
        long materialPrevious = Math.max(0L, materialLast14 - materialRecent);

        long aiRecent = aiInteractionRepository.countByStudentIdAndCourseIdAndCreatedAtAfter(
                studentId,
                courseId,
                recentCutoff
        );
        long aiLast14 = aiInteractionRepository.countByStudentIdAndCourseIdAndCreatedAtAfter(
                studentId,
                courseId,
                previousCutoff
        );
        long aiPrevious = Math.max(0L, aiLast14 - aiRecent);

        double recentActivityIndex =
                (attendanceRecent * 3.0)
                        + (materialRecent * 1.5)
                        + (aiRecent * 1.5);

        double previousActivityIndex =
                (attendancePrevious * 3.0)
                        + (materialPrevious * 1.5)
                        + (aiPrevious * 1.5);

        if (recentActivityIndex < 1.0 && previousActivityIndex < 1.0) {
            return "STABLE";
        }
        if (previousActivityIndex == 0.0 && recentActivityIndex > 0.0) {
            return "IMPROVING";
        }
        if (recentActivityIndex >= previousActivityIndex * 1.2) {
            return "IMPROVING";
        }
        if (recentActivityIndex <= previousActivityIndex * 0.8) {
            return "DECLINING";
        }
        return "STABLE";
    }

    private String calculateStatus(double progressScore, DropoutPrediction latestPrediction) {
        String baseStatus;
        if (progressScore >= 75.0) {
            baseStatus = "ON_TRACK";
        } else if (progressScore >= 50.0) {
            baseStatus = "NEEDS_ATTENTION";
        } else {
            baseStatus = "AT_RISK";
        }

        if (latestPrediction != null
                && latestPrediction.getRiskLevel() != null
                && "HIGH".equalsIgnoreCase(latestPrediction.getRiskLevel())
                && progressScore < 70.0) {
            return "AT_RISK";
        }

        return baseStatus;
    }

    private List<String> buildReasons(
            long attendancePresentCountWindow,
            long materialViewsCountWindow,
            long aiTotalCountWindow,
            long daysSinceLastLogin,
            long daysSinceLastAttendance,
            long daysSinceLastMaterialView,
            long daysSinceLastAiUse,
            EngagementWindowMetrics engagementMetrics,
            DropoutPrediction latestPrediction,
            String trend
    ) {
        List<String> reasons = new ArrayList<>();

        if (attendancePresentCountWindow <= 1 || daysSinceLastAttendance > 14) {
            reasons.add("Recent attendance activity is low.");
        } else if (attendancePresentCountWindow >= 4) {
            reasons.add("Recent attendance activity is stable.");
        }

        if (materialViewsCountWindow == 0 || daysSinceLastMaterialView > 14) {
            reasons.add("Learning material interaction is limited.");
        } else if (materialViewsCountWindow >= 3) {
            reasons.add("Learning material interaction is active.");
        }

        if (aiTotalCountWindow == 0 || daysSinceLastAiUse > 14) {
            reasons.add("AI-supported learning activity is limited.");
        } else if (aiTotalCountWindow >= 2) {
            reasons.add("AI learning support usage is active.");
        }

        if (daysSinceLastLogin > 14) {
            reasons.add("Platform login activity has declined recently.");
        }

        if (engagementMetrics.averageEngagementScore() != null) {
            if (engagementMetrics.averageEngagementScore() < 0.45) {
                reasons.add("Recent engagement evidence is weak.");
            } else if (engagementMetrics.averageEngagementScore() >= 0.65) {
                reasons.add("Recent engagement evidence is positive.");
            }
        }

        if (latestPrediction != null && latestPrediction.getRiskLevel() != null) {
            reasons.add("Latest dropout analytics indicates " + latestPrediction.getRiskLevel().toUpperCase() + " risk.");
        }

        reasons.add("Recent learning trend is " + trend + ".");

        return reasons.stream().distinct().toList();
    }

    private String buildInterventionSuggestion(
            long attendancePresentCountWindow,
            long materialViewsCountWindow,
            long aiTotalCountWindow,
            long daysSinceLastLogin,
            long daysSinceLastAttendance,
            long daysSinceLastMaterialView,
            Double averageEngagementScore,
            DropoutPrediction latestPrediction,
            double progressScore
    ) {
        String riskLevel = latestPrediction != null ? latestPrediction.getRiskLevel() : null;

        if ("HIGH".equalsIgnoreCase(riskLevel) || progressScore < 50.0) {
            return "Prioritized academic monitoring and teacher follow-up are recommended.";
        }

        if (attendancePresentCountWindow <= 1 || daysSinceLastAttendance > 14) {
            return "Increase attendance consistency in upcoming class sessions.";
        }

        if (materialViewsCountWindow == 0 || daysSinceLastMaterialView > 14) {
            return "Review course materials more regularly to maintain learning continuity.";
        }

        if (daysSinceLastLogin > 14) {
            return "Resume regular platform use and revisit current course content.";
        }

        if (averageEngagementScore != null && averageEngagementScore < 0.45) {
            return "Teacher follow-up and additional learning support are recommended.";
        }

        if (aiTotalCountWindow == 0) {
            return "Use the available AI learning support tools more consistently for revision and guidance.";
        }

        return "Maintain current learning consistency and course participation.";
    }

    private ProgressAnalyticsResponse toResponse(StudentProgressAnalytics item) {
        return new ProgressAnalyticsResponse(
                item.getStudentId(),
                item.getStudentName(),
                item.getStudentEmail(),
                item.getCourseId(),
                item.getCourseCode(),
                item.getCourseTitle(),
                item.getWindowDays(),
                item.getProgressScore(),
                item.getTrend(),
                item.getStatus(),
                item.getInterventionSuggestion(),
                item.getDropoutRiskLevel(),
                item.getDropoutProbability(),
                item.getAttendancePresentCountWindow(),
                item.getMaterialViewsCountWindow(),
                item.getAiTotalCountWindow(),
                item.getDaysSinceLastLogin(),
                item.getDaysSinceLastAttendance(),
                item.getDaysSinceLastMaterialView(),
                item.getDaysSinceLastAiUse(),
                item.getAverageEngagementScore(),
                item.getAttentiveCount(),
                item.getNeutralCount(),
                item.getDistractedCount(),
                item.getReasons(),
                item.getGeneratedAt()
        );
    }

    private String resolveRole(Authentication authentication) {
        String authority = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("UNKNOWN");

        return authority.startsWith("ROLE_")
                ? authority.substring(5)
                : authority;
    }

    private long asLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (Exception ex) {
            return 0L;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record EngagementWindowMetrics(
            Double averageEngagementScore,
            long attentiveCount,
            long neutralCount,
            long distractedCount
    ) {}
}