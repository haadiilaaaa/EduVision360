package lk.icbt.eduvision.eduvision360.prediction.repository;

import lk.icbt.eduvision.eduvision360.prediction.model.DropoutPrediction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DropoutPredictionRepository extends MongoRepository<DropoutPrediction, String> {

    Optional<DropoutPrediction> findByStudentIdAndCourseId(String studentId, String courseId);

    long countByRiskLevel(String riskLevel);

    long countByTeacherIdAndRiskLevel(String teacherId, String riskLevel);

    List<DropoutPrediction> findTop10ByOrderByPredictedAtDesc();

    List<DropoutPrediction> findTop10ByTeacherIdOrderByPredictedAtDesc(String teacherId);

    List<DropoutPrediction> findByTeacherIdAndCourseIdOrderByPredictedAtDesc(String teacherId, String courseId);

    List<DropoutPrediction> findAllByOrderByPredictedAtDesc();

    // old student-id queries
    Optional<DropoutPrediction> findTopByStudentIdOrderByPredictedAtDesc(String studentId);
    List<DropoutPrediction> findByStudentIdOrderByPredictedAtDesc(String studentId);

    // NEW email-based queries for student self-view
    Optional<DropoutPrediction> findTopByStudentEmailOrderByPredictedAtDesc(String studentEmail);
    List<DropoutPrediction> findByStudentEmailOrderByPredictedAtDesc(String studentEmail);
}