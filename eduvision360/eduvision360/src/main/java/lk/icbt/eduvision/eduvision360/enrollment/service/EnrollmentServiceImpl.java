package lk.icbt.eduvision.eduvision360.enrollment.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.department.model.Department;
import lk.icbt.eduvision.eduvision360.department.repository.DepartmentRepository;
import lk.icbt.eduvision.eduvision360.enrollment.dto.EnrollCourseRequest;
import lk.icbt.eduvision.eduvision360.enrollment.dto.EnrollmentResponse;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.EmailService;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public EnrollmentResponse enroll(EnrollCourseRequest req, Authentication authentication) {

        String studentId = authentication.getName();

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, course.getId())) {
            throw new IllegalStateException("Already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(student.getId())
                .studentEmail(student.getEmail())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .enrolledAt(Instant.now())
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        // DB notification for student
        notificationService.createNotification(
                student.getId(),
                "Course Enrollment Successful",
                "You have successfully enrolled in " + course.getCourseCode() + " - " + course.getTitle() + ".",
                NotificationType.COURSE_ENROLLED,
                course.getId(),
                "COURSE",
                "/student/my-courses"
        );

        // DB notification for assigned teacher
        if (course.getTeacherId() != null && !course.getTeacherId().isBlank()) {
            notificationService.createNotification(
                    course.getTeacherId(),
                    "New Student Enrolled",
                    student.getFullName() + " has enrolled in " + course.getCourseCode() + " - " + course.getTitle() + ".",
                    NotificationType.NEW_STUDENT_ENROLLED,
                    course.getId(),
                    "COURSE",
                    "/teacher/courses"
            );
        }

        // Email to student
        try {
            emailService.sendEmail(
                    student.getEmail(),
                    "Enrollment Confirmation - " + course.getCourseCode(),
                    "Hello " + student.getFullName() + ",\n\n"
                            + "You have successfully enrolled in the course:\n"
                            + course.getCourseCode() + " - " + course.getTitle() + "\n\n"
                            + "Please check your student dashboard for course details.\n\n"
                            + "Regards,\nEduVision 360"
            );
        } catch (Exception ex) {
            System.out.println("Student enrollment email failed: " + ex.getMessage());
        }

        // Email to teacher
        if (course.getTeacherId() != null && !course.getTeacherId().isBlank()) {
            try {
                userRepository.findById(course.getTeacherId()).ifPresent(teacher -> {
                    try {
                        emailService.sendEmail(
                                teacher.getEmail(),
                                "New Student Enrolled - " + course.getCourseCode(),
                                "Hello " + teacher.getFullName() + ",\n\n"
                                        + student.getFullName() + " has enrolled in your course:\n"
                                        + course.getCourseCode() + " - " + course.getTitle() + "\n\n"
                                        + "Please check your teacher dashboard for updates.\n\n"
                                        + "Regards,\nEduVision 360"
                        );
                    } catch (Exception ex) {
                        System.out.println("Teacher enrollment email failed: " + ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                System.out.println("Teacher lookup/email failed: " + ex.getMessage());
            }
        }

        return toResponse(saved, course);
    }

    @Override
    public List<EnrollmentResponse> myEnrollments(Authentication authentication) {
        String studentId = authentication.getName();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);

        return enrollments.stream().map(e -> {
            Course course = courseRepository.findById(e.getCourseId()).orElse(null);
            return toResponse(e, course);
        }).toList();
    }

    private EnrollmentResponse toResponse(Enrollment e, Course course) {
        if (course == null) {
            return new EnrollmentResponse(
                    e.getId(),
                    e.getCourseId(),
                    e.getCourseCode(),
                    "(Course Deleted)",
                    null,
                    null,
                    e.getEnrolledAt()
            );
        }

        String departmentName = departmentRepository.findById(course.getDepartmentId())
                .map(Department::getName)
                .orElse(null);

        String teacherName = (course.getTeacherId() == null) ? null :
                userRepository.findById(course.getTeacherId()).map(User::getFullName).orElse(null);

        return new EnrollmentResponse(
                e.getId(),
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                departmentName,
                teacherName,
                e.getEnrolledAt()
        );
    }
}