package lk.icbt.eduvision.eduvision360.feedback.repository;

import lk.icbt.eduvision.eduvision360.feedback.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedbackRepository extends MongoRepository<Feedback, String> {

    List<Feedback> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<Feedback> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    List<Feedback> findByCourseIdOrderByCreatedAtDesc(String courseId);

    List<Feedback> findAllByOrderByCreatedAtDesc();
}