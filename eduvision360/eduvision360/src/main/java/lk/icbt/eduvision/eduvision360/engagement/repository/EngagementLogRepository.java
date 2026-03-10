package lk.icbt.eduvision.eduvision360.engagement.repository;

import lk.icbt.eduvision.eduvision360.engagement.model.EngagementLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface EngagementLogRepository extends MongoRepository<EngagementLog, String> {

    List<EngagementLog> findTop10ByOrderByCapturedAtDesc();

    List<EngagementLog> findTop10ByCourseIdOrderByCapturedAtDesc(String courseId);

    List<EngagementLog> findTop10ByStudentIdOrderByCapturedAtDesc(String studentId);

    List<EngagementLog> findByCourseIdOrderByCapturedAtDesc(String courseId);

    List<EngagementLog> findByStudentIdOrderByCapturedAtDesc(String studentId);

    List<EngagementLog> findByStudentIdAndCourseIdOrderByCapturedAtDesc(String studentId, String courseId);

    List<EngagementLog> findByStudentIdAndCourseIdAndCapturedAtAfterOrderByCapturedAtDesc(
            String studentId,
            String courseId,
            Instant after
    );

    long countByLabel(String label);

    long countByCourseIdAndLabel(String courseId, String label);
}