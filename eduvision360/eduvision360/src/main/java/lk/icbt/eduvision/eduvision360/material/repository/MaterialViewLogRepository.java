package lk.icbt.eduvision.eduvision360.material.repository;

import lk.icbt.eduvision.eduvision360.material.model.MaterialViewLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MaterialViewLogRepository extends MongoRepository<MaterialViewLog, String> {

    List<MaterialViewLog> findByStudentIdOrderByViewedAtDesc(String studentId);

    List<MaterialViewLog> findByCourseIdOrderByViewedAtDesc(String courseId);

    long countByStudentId(String studentId);

    long countByStudentIdAndCourseId(String studentId, String courseId);

    long countByMaterialId(String materialId);

    long countByStudentIdAndCourseIdAndViewedAtAfter(String studentId, String courseId, Instant after);

    Optional<MaterialViewLog> findTopByStudentIdAndCourseIdOrderByViewedAtDesc(String studentId, String courseId);
}