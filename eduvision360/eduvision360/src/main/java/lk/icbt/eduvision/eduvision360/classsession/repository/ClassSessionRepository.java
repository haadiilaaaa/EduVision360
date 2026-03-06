package lk.icbt.eduvision.eduvision360.classsession.repository;

import lk.icbt.eduvision.eduvision360.classsession.model.ClassSession;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassSessionRepository extends MongoRepository<ClassSession, String> {

    List<ClassSession> findByTeacherIdOrderBySessionDateDescStartTimeDesc(String teacherId);

    List<ClassSession> findByCourseIdOrderBySessionDateDescStartTimeDesc(String courseId);

    List<ClassSession> findByCourseIdInOrderBySessionDateDescStartTimeDesc(List<String> courseIds);

    List<ClassSession> findByStatusOrderBySessionDateDescStartTimeDesc(ClassSessionStatus status);

    List<ClassSession> findByTeacherIdAndStatusOrderBySessionDateDescStartTimeDesc(
            String teacherId,
            ClassSessionStatus status
    );

    List<ClassSession> findByCourseIdAndSessionDateOrderByStartTimeAsc(String courseId, LocalDate sessionDate);
}