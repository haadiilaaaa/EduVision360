package lk.icbt.eduvision.eduvision360.classsession.service;

import lk.icbt.eduvision.eduvision360.attendance.model.Attendance;
import lk.icbt.eduvision.eduvision360.attendance.model.AttendanceStatus;
import lk.icbt.eduvision.eduvision360.attendance.repository.AttendanceRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.classsession.dto.ClassSessionResponse;
import lk.icbt.eduvision.eduvision360.classsession.dto.CreateClassSessionRequest;
import lk.icbt.eduvision.eduvision360.classsession.dto.UpdateClassSessionStatusRequest;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSession;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;
import lk.icbt.eduvision.eduvision360.classsession.repository.ClassSessionRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.EmailService;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassSessionServiceImpl implements ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public ClassSessionResponse createSession(CreateClassSessionRequest request, Authentication authentication) {
        String authUserId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("UNKNOWN");

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if ("TEACHER".equals(role)) {
            if (course.getTeacherId() == null || !course.getTeacherId().equals(authUserId)) {
                throw new AccessDeniedException("You can only create sessions for your assigned courses");
            }
        } else if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not allowed to create class sessions");
        }

        User teacher = null;
        if (course.getTeacherId() != null) {
            teacher = userRepository.findById(course.getTeacherId()).orElse(null);
        }

        ClassSession session = ClassSession.builder()
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .teacherId(course.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .sessionDate(request.sessionDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(ClassSessionStatus.SCHEDULED)
                .build();

        ClassSession saved = classSessionRepository.save(session);

        // Notify enrolled students in DB + email
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
        for (Enrollment enrollment : enrollments) {
            notificationService.createNotification(
                    enrollment.getStudentId(),
                    "New Class Session Scheduled",
                    "A new session has been scheduled for " + course.getCourseCode() + " - " + course.getTitle()
                            + " on " + saved.getSessionDate() + " from " + saved.getStartTime() + " to " + saved.getEndTime() + ".",
                    NotificationType.CLASS_SESSION_CREATED,
                    NotificationPriority.MEDIUM,
                    saved.getId(),
                    "CLASS_SESSION",
                    "/student/sessions"
            );

            if (enrollment.getStudentEmail() != null && !enrollment.getStudentEmail().isBlank()) {
                emailService.sendEmail(
                        enrollment.getStudentEmail(),
                        "Class Session Scheduled - " + course.getCourseCode(),
                        "Hello,\n\n"
                                + "A new class session has been scheduled for:\n"
                                + course.getCourseCode() + " - " + course.getTitle() + "\n\n"
                                + "Date: " + saved.getSessionDate() + "\n"
                                + "Time: " + saved.getStartTime() + " to " + saved.getEndTime() + "\n\n"
                                + "Please check your student dashboard for full details.\n\n"
                                + "Regards,\nEduVision 360",
                        "CLASS_SESSION_CREATED",
                        saved.getId(),
                        "CLASS_SESSION"
                );
            }
        }

        return toResponse(saved);
    }

    @Override
    public List<ClassSessionResponse> getTeacherSessions(Authentication authentication) {
        String teacherId = authentication.getName();

        return classSessionRepository.findByTeacherIdOrderBySessionDateDescStartTimeDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ClassSessionResponse> getStudentSessions(Authentication authentication) {
        String studentId = authentication.getName();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);
        List<String> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();

        if (courseIds.isEmpty()) return List.of();

        return classSessionRepository.findByCourseIdInOrderBySessionDateDescStartTimeDesc(courseIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ClassSessionResponse updateSessionStatus(
            String sessionId,
            UpdateClassSessionStatusRequest request,
            Authentication authentication
    ) {
        String authUserId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("UNKNOWN");

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session not found"));

        if ("TEACHER".equals(role)) {
            if (session.getTeacherId() == null || !session.getTeacherId().equals(authUserId)) {
                throw new AccessDeniedException("You can only update your own class sessions");
            }
        } else if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("You are not allowed to update class sessions");
        }

        ClassSession saved = updateStatusInternal(session, request.status());
        return toResponse(saved);
    }

    /**
     * ✅ SYSTEM update (Scheduler) - no Authentication
     */
    public void updateSessionStatusSystem(String sessionId, ClassSessionStatus newStatus) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session not found"));

        updateStatusInternal(session, newStatus);
    }

    /**
     * ✅ Closing soon warnings (Scheduler)
     * Requires NotificationType.CLASS_SESSION_CLOSING_SOON
     */
    public void sendClosingSoonWarningsSystem(String sessionId, long minutesLeft) {
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session not found"));

        if (session.getStatus() != ClassSessionStatus.OPEN) return;

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(session.getCourseId());
        List<Attendance> attendanceList = attendanceRepository.findBySessionId(session.getId());

        Set<String> presentStudentIds = attendanceList.stream()
                .filter(a -> a.getStudentId() != null)
                .filter(a -> a.getStatus() == null || !a.getStatus().name().equalsIgnoreCase("ABSENT"))
                .map(Attendance::getStudentId)
                .collect(Collectors.toSet());

        List<Enrollment> missing = enrollments.stream()
                .filter(e -> e.getStudentId() != null && !presentStudentIds.contains(e.getStudentId()))
                .toList();

        for (Enrollment e : missing) {
            notificationService.createNotificationWithCooldown(
                    e.getStudentId(),
                    "Attendance closing soon",
                    "Attendance will close in ~" + minutesLeft + " minute(s) for "
                            + session.getCourseCode() + " on " + session.getSessionDate()
                            + ". Please mark now.",
                    NotificationType.CLASS_SESSION_CLOSING_SOON,
                    session.getId(),
                    "CLASS_SESSION",
                    "/student/attendance/" + session.getId(),
                    Duration.ofMinutes(30)
            );
        }
    }

    // =========================
    // Internal helpers
    // =========================

    private ClassSession updateStatusInternal(ClassSession session, ClassSessionStatus newStatus) {
        ClassSessionStatus oldStatus = session.getStatus();
        if (oldStatus == newStatus) return session;

        // Optional safety: don't allow automation to change CANCELLED sessions
        if (oldStatus == ClassSessionStatus.CANCELLED) return session;

        session.setStatus(newStatus);
        ClassSession saved = classSessionRepository.save(session);

        if (oldStatus != ClassSessionStatus.OPEN && newStatus == ClassSessionStatus.OPEN) {
            handleSessionOpened(saved);
        }

        if (oldStatus != ClassSessionStatus.COMPLETED && newStatus == ClassSessionStatus.COMPLETED) {
            handleSessionCompleted(saved);
        }

        return saved;
    }

    private void handleSessionOpened(ClassSession saved) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(saved.getCourseId());

        for (Enrollment enrollment : enrollments) {
            notificationService.createNotification(
                    enrollment.getStudentId(),
                    "Attendance is Now Open",
                    "Attendance is now open for " + saved.getCourseCode() + " - " + saved.getCourseTitle()
                            + ". Please mark your attendance for the session on " + saved.getSessionDate() + ".",
                    NotificationType.CLASS_SESSION_OPENED,
                    NotificationPriority.HIGH,
                    saved.getId(),
                    "CLASS_SESSION",
                    "/student/attendance/" + saved.getId()
            );

            if (enrollment.getStudentEmail() != null && !enrollment.getStudentEmail().isBlank()) {
                emailService.sendEmail(
                        enrollment.getStudentEmail(),
                        "Attendance Open - " + saved.getCourseCode(),
                        "Hello,\n\n"
                                + "Attendance is now OPEN for:\n"
                                + saved.getCourseCode() + " - " + saved.getCourseTitle() + "\n\n"
                                + "Date: " + saved.getSessionDate() + "\n"
                                + "Time: " + saved.getStartTime() + " to " + saved.getEndTime() + "\n\n"
                                + "Please mark your attendance from the student dashboard.\n\n"
                                + "Regards,\nEduVision 360",
                        "CLASS_SESSION_OPENED",
                        saved.getId(),
                        "CLASS_SESSION"
                );
            }
        }
    }

    private void handleSessionCompleted(ClassSession saved) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(saved.getCourseId());
        List<Attendance> attendanceList = attendanceRepository.findBySessionId(saved.getId());

        Set<String> presentStudentIds = attendanceList.stream()
                .filter(a -> a.getStudentId() != null)
                .filter(a -> a.getStatus() == null || !a.getStatus().name().equalsIgnoreCase("ABSENT"))
                .map(Attendance::getStudentId)
                .collect(Collectors.toSet());

        List<Enrollment> absent = enrollments.stream()
                .filter(e -> e.getStudentId() != null && !presentStudentIds.contains(e.getStudentId()))
                .toList();

        int presentCount = presentStudentIds.size();
        int absentCount = absent.size();

        // ✅ Create ABSENT attendance records so attendance-history shows correctly
        for (Enrollment e : absent) {
            if (e.getStudentId() == null) continue;

            boolean alreadyExists = attendanceRepository.existsByStudentIdAndSessionId(e.getStudentId(), saved.getId());
            if (alreadyExists) continue;

            Attendance absentRecord = Attendance.builder()
                    .studentId(e.getStudentId())
                    .studentEmail(e.getStudentEmail())
                    .sessionId(saved.getId())
                    .courseId(saved.getCourseId())
                    .courseCode(saved.getCourseCode())
                    .classId(saved.getCourseCode()) // legacy alias = courseCode
                    .date(saved.getSessionDate())
                    .time(saved.getEndTime() != null ? saved.getEndTime() : saved.getStartTime())
                    .status(AttendanceStatus.ABSENT)
                    .confidence(null)
                    .build();

            attendanceRepository.save(absentRecord);
        }

        // Teacher summary (in-app + optional email)
        if (saved.getTeacherId() != null) {
            String summaryMsg = "Session completed for " + saved.getCourseCode() + " on " + saved.getSessionDate()
                    + ". Present: " + presentCount + ", Absent: " + absentCount + ".";

            notificationService.createNotificationWithCooldown(
                    saved.getTeacherId(),
                    "Session completed summary",
                    summaryMsg,
                    NotificationType.CLASS_SESSION_COMPLETED,
                    saved.getId(),
                    "CLASS_SESSION",
                    "/teacher/attendance/" + saved.getId(),
                    Duration.ofHours(6)
            );

            User teacher = userRepository.findById(saved.getTeacherId()).orElse(null);
            if (teacher != null && teacher.getEmail() != null && !teacher.getEmail().isBlank()) {
                emailService.sendEmail(
                        teacher.getEmail(),
                        "EduVision360: Session Completed Summary - " + saved.getCourseCode(),
                        "Hello " + (teacher.getFullName() != null ? teacher.getFullName() : "") + ",\n\n"
                                + summaryMsg + "\n\nRegards,\nEduVision360",
                        "CLASS_SESSION_COMPLETED",
                        saved.getId(),
                        "CLASS_SESSION"
                );
            }
        }

        // Absent students → notification + email
        for (Enrollment enrollment : absent) {

            notificationService.createNotificationWithCooldown(
                    enrollment.getStudentId(),
                    "Attendance missing",
                    "No attendance record was found for your session " + saved.getCourseCode()
                            + " on " + saved.getSessionDate()
                            + ". If this is incorrect, please contact your teacher.",
                    NotificationType.ATTENDANCE_MISSED,
                    saved.getId(),
                    "CLASS_SESSION",
                    "/student/attendance-history",
                    Duration.ofDays(7)
            );

            if (enrollment.getStudentEmail() != null && !enrollment.getStudentEmail().isBlank()) {
                emailService.sendEmail(
                        enrollment.getStudentEmail(),
                        "EduVision360: Attendance Missing - " + saved.getCourseCode(),
                        "Hello,\n\n"
                                + "No attendance record was found for your session:\n"
                                + saved.getCourseCode() + " - " + saved.getCourseTitle() + "\n\n"
                                + "Date: " + saved.getSessionDate() + "\n"
                                + "Time: " + saved.getStartTime() + " to " + saved.getEndTime() + "\n\n"
                                + "If this is incorrect, please contact your teacher.\n\n"
                                + "Regards,\nEduVision360",
                        "ATTENDANCE_MISSED",
                        saved.getId(),
                        "CLASS_SESSION"
                );
            }
        }
    }

    private ClassSessionResponse toResponse(ClassSession session) {
        return new ClassSessionResponse(
                session.getId(),
                session.getCourseId(),
                session.getCourseCode(),
                session.getCourseTitle(),
                session.getTeacherId(),
                session.getTeacherName(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus(),
                session.getCreatedAt()
        );
    }
}