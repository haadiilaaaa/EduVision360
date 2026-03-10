package lk.icbt.eduvision.eduvision360.progress.repository;

import lk.icbt.eduvision.eduvision360.progress.model.StudentProgressAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudentProgressAnalyticsRepository extends MongoRepository<StudentProgressAnalytics, String> {

    Optional<StudentProgressAnalytics> findByStudentIdAndCourseId(String studentId, String courseId);

    Optional<StudentProgressAnalytics> findTopByStudentIdAndCourseIdOrderByGeneratedAtDesc(
            String studentId,
            String courseId
    );

    List<StudentProgressAnalytics> findByStudentIdOrderByGeneratedAtDesc(String studentId);

    List<StudentProgressAnalytics> findByCourseIdOrderByGeneratedAtDesc(String courseId);

    List<StudentProgressAnalytics> findAllByOrderByGeneratedAtDesc();
}