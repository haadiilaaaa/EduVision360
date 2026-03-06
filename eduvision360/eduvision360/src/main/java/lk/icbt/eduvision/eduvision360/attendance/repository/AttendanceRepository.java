package lk.icbt.eduvision.eduvision360.attendance.repository;

import lk.icbt.eduvision.eduvision360.attendance.model.Attendance;
import lk.icbt.eduvision.eduvision360.attendance.model.AttendanceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    boolean existsByStudentIdAndSessionId(String studentId, String sessionId);

    List<Attendance> findByStudentIdOrderByDateDescTimeDesc(String studentId);

    List<Attendance> findByStudentIdAndCourseIdOrderByDateDescTimeDesc(String studentId, String courseId);

    List<Attendance> findBySessionId(String sessionId);

    List<Attendance> findByCourseIdIn(List<String> courseIds);

    List<Attendance> findByCourseIdInAndDate(List<String> courseIds, LocalDate date);

    List<Attendance> findByCourseIdAndDate(String courseId, LocalDate date);

    long countByStudentId(String studentId);

    long countByStudentIdAndStatus(String studentId, AttendanceStatus status);

    long countByStudentIdAndCourseId(String studentId, String courseId);

    long countByStudentIdAndCourseIdAndStatus(String studentId, String courseId, AttendanceStatus status);

    Optional<Attendance> findTopByStudentIdOrderByDateDescTimeDesc(String studentId);
}