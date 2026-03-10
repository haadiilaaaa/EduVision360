package lk.icbt.eduvision.eduvision360.quiz.repository;

import lk.icbt.eduvision.eduvision360.quiz.model.TeacherQuiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherQuizRepository extends MongoRepository<TeacherQuiz, String> {

    List<TeacherQuiz> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    List<TeacherQuiz> findByTeacherIdAndCourseIdOrderByCreatedAtDesc(String teacherId, String courseId);

    Optional<TeacherQuiz> findByIdAndTeacherId(String id, String teacherId);
    List<TeacherQuiz> findTop5ByOrderByCreatedAtDesc();
}