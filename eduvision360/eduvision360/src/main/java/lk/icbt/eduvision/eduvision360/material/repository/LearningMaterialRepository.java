package lk.icbt.eduvision.eduvision360.material.repository;

import lk.icbt.eduvision.eduvision360.material.model.LearningMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LearningMaterialRepository extends MongoRepository<LearningMaterial, String> {

    List<LearningMaterial> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    List<LearningMaterial> findByTeacherIdAndCourseIdOrderByCreatedAtDesc(String teacherId, String courseId);

    List<LearningMaterial> findByCourseIdOrderByCreatedAtDesc(String courseId);

    List<LearningMaterial> findByCourseIdInOrderByCreatedAtDesc(List<String> courseIds);

    Optional<LearningMaterial> findByIdAndTeacherId(String id, String teacherId);

    List<LearningMaterial> findTop5ByOrderByCreatedAtDesc();
}