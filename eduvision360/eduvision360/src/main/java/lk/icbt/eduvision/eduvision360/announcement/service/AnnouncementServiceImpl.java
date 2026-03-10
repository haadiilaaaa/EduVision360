package lk.icbt.eduvision.eduvision360.announcement.service;

import lk.icbt.eduvision.eduvision360.announcement.dto.AnnouncementResponse;
import lk.icbt.eduvision.eduvision360.announcement.dto.CreateAnnouncementRequest;
import lk.icbt.eduvision.eduvision360.announcement.model.CourseAnnouncement;
import lk.icbt.eduvision.eduvision360.announcement.repository.AnnouncementRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public AnnouncementResponse create(CreateAnnouncementRequest req, String teacherId) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only post announcements for your assigned courses");
        }

        CourseAnnouncement announcement = CourseAnnouncement.builder()
                .courseId(course.getId())
                .teacherId(teacherId)
                .title(req.getTitle().trim())
                .message(req.getMessage().trim())
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();

        CourseAnnouncement saved = announcementRepository.save(announcement);

        // Create student dashboard notifications
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdOrderByEnrolledAtDesc(course.getId());

        for (Enrollment enrollment : enrollments) {
            notificationService.createNotificationWithCooldown(
                    enrollment.getStudentId(),
                    "New announcement posted",
                    "A new announcement was posted for " + course.getCourseCode() + " - " + course.getTitle() + ".",
                    NotificationType.ANNOUNCEMENT_POSTED,
                    saved.getId(),
                    "ANNOUNCEMENT",
                    "/student/announcements",
                    Duration.ofMinutes(30)
            );
        }

        return toResponse(saved);
    }

    @Override
    public AnnouncementResponse updateTeacherAnnouncement(String teacherId, String announcementId, CreateAnnouncementRequest req) {
        CourseAnnouncement announcement = announcementRepository.findByIdAndTeacherId(announcementId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found or access denied"));

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only update announcements for your assigned courses");
        }

        announcement.setCourseId(course.getId());
        announcement.setTitle(req.getTitle().trim());
        announcement.setMessage(req.getMessage().trim());
        announcement.setUpdatedAt(Instant.now());

        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    public List<AnnouncementResponse> getTeacherAnnouncements(String teacherId) {
        return announcementRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AnnouncementResponse> getTeacherAnnouncementsByCourse(String teacherId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("You do not have access to this course");
        }

        return announcementRepository.findByTeacherIdAndCourseIdOrderByCreatedAtDesc(teacherId, courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteTeacherAnnouncement(String teacherId, String announcementId) {
        CourseAnnouncement announcement = announcementRepository.findByIdAndTeacherId(announcementId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found or access denied"));

        announcementRepository.delete(announcement);
    }

    @Override
    public List<AnnouncementResponse> getStudentAnnouncementsForCourse(String studentId, String courseId) {
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!enrolled) {
            throw new IllegalArgumentException("You are not enrolled in this course");
        }

        return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AnnouncementResponse> getStudentAnnouncementsForMyCourses(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);

        List<String> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return announcementRepository.findByCourseIdInOrderByCreatedAtDesc(courseIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AnnouncementResponse toResponse(CourseAnnouncement announcement) {
        Course course = courseRepository.findById(announcement.getCourseId()).orElse(null);

        String courseCode = course != null ? course.getCourseCode() : null;
        String courseTitle = course != null ? course.getTitle() : null;

        String teacherName = userRepository.findById(announcement.getTeacherId())
                .map(User::getFullName)
                .orElse(null);

        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getCourseId(),
                courseCode,
                courseTitle,
                announcement.getTeacherId(),
                teacherName,
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }
}