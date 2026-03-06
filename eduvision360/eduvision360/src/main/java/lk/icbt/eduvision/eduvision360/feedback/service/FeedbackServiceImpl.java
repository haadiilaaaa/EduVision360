package lk.icbt.eduvision.eduvision360.feedback.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.feedback.dto.CreateFeedbackRequest;
import lk.icbt.eduvision.eduvision360.feedback.dto.FeedbackResponse;
import lk.icbt.eduvision.eduvision360.feedback.dto.UpdateFeedbackStatusRequest;
import lk.icbt.eduvision.eduvision360.feedback.model.Feedback;
import lk.icbt.eduvision.eduvision360.feedback.repository.FeedbackRepository;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    @Override
    public FeedbackResponse submitFeedback(CreateFeedbackRequest request, Authentication authentication) {
        String studentId = authentication.getName();

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId());
        if (!enrolled) {
            throw new AccessDeniedException("You can only submit feedback for your enrolled courses");
        }

        Feedback feedback = Feedback.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .teacherId(course.getTeacherId())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .sessionId(request.sessionId())
                .category(request.category())
                .rating(request.rating())
                .comment(request.comment().trim())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        if (course.getTeacherId() != null && !course.getTeacherId().isBlank()) {
            notificationService.createNotification(
                    course.getTeacherId(),
                    "New Student Feedback Submitted",
                    student.getFullName() + " submitted feedback for " + course.getCourseCode() + " - " + course.getTitle() + ".",
                    NotificationType.SYSTEM_ALERT,
                    saved.getId(),
                    "FEEDBACK",
                    "/teacher/feedback"
            );
        }

        return toResponse(saved);
    }

    @Override
    public List<FeedbackResponse> getMyFeedback(Authentication authentication) {
        String studentId = authentication.getName();
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<FeedbackResponse> getTeacherFeedback(Authentication authentication) {
        String teacherId = authentication.getName();
        return feedbackRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<FeedbackResponse> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FeedbackResponse updateTeacherFeedbackStatus(
            String feedbackId,
            UpdateFeedbackStatusRequest request,
            Authentication authentication
    ) {
        String teacherId = authentication.getName();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        if (feedback.getTeacherId() == null || !feedback.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("You can only update feedback for your own courses");
        }

        User responder = userRepository.findById(teacherId).orElse(null);

        feedback.setStatus(request.status());
        feedback.setResponseComment(request.responseComment());
        feedback.setResponderId(teacherId);
        feedback.setResponderName(responder != null ? responder.getFullName() : "Teacher");
        feedback.setUpdatedAt(Instant.now());

        Feedback saved = feedbackRepository.save(feedback);

        notificationService.createNotification(
                feedback.getStudentId(),
                "Feedback Updated",
                "Your feedback for " + feedback.getCourseCode() + " - " + feedback.getCourseTitle() + " has been reviewed.",
                NotificationType.SYSTEM_ALERT,
                saved.getId(),
                "FEEDBACK",
                "/student/feedback"
        );

        return toResponse(saved);
    }

    @Override
    public FeedbackResponse updateAdminFeedbackStatus(
            String feedbackId,
            UpdateFeedbackStatusRequest request,
            Authentication authentication
    ) {
        String adminId = authentication.getName();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        User responder = userRepository.findById(adminId).orElse(null);

        feedback.setStatus(request.status());
        feedback.setResponseComment(request.responseComment());
        feedback.setResponderId(adminId);
        feedback.setResponderName(responder != null ? responder.getFullName() : "Admin");
        feedback.setUpdatedAt(Instant.now());

        Feedback saved = feedbackRepository.save(feedback);

        notificationService.createNotification(
                feedback.getStudentId(),
                "Feedback Updated",
                "Your feedback for " + feedback.getCourseCode() + " - " + feedback.getCourseTitle() + " has been reviewed by administration.",
                NotificationType.SYSTEM_ALERT,
                saved.getId(),
                "FEEDBACK",
                "/student/feedback"
        );

        return toResponse(saved);
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getStudentId(),
                feedback.getStudentName(),
                feedback.getStudentEmail(),
                feedback.getTeacherId(),
                feedback.getCourseId(),
                feedback.getCourseCode(),
                feedback.getCourseTitle(),
                feedback.getSessionId(),
                feedback.getCategory(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getStatus(),
                feedback.getResponseComment(),
                feedback.getResponderId(),
                feedback.getResponderName(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }
}