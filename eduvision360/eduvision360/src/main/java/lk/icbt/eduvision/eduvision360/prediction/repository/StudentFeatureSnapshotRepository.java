package lk.icbt.eduvision.eduvision360.prediction.repository;

import lk.icbt.eduvision.eduvision360.prediction.model.StudentFeatureSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StudentFeatureSnapshotRepository extends MongoRepository<StudentFeatureSnapshot, String> {

    Optional<StudentFeatureSnapshot> findTopByStudentIdAndCourseIdAndWindowDaysOrderByCreatedAtDesc(
            String studentId,
            String courseId,
            Integer windowDays
    );
}