package lk.icbt.eduvision.eduvision360.enrollment.repository;

import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    boolean existsByStudentIdAndCourseId(String studentId, String courseId);

    List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(String studentId);

    Optional<Enrollment> findByStudentIdAndCourseCodeIgnoreCase(String studentId, String courseCode);

    List<Enrollment> findByCourseIdOrderByEnrolledAtDesc(String courseId);

    List<Enrollment> findByCourseId(String courseId);

    List<Enrollment> findByEnrolledAtBetween(Instant start, Instant end);
}