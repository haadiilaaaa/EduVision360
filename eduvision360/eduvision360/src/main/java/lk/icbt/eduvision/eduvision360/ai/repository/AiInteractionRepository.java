package lk.icbt.eduvision.eduvision360.ai.repository;

import lk.icbt.eduvision.eduvision360.ai.model.AiInteraction;
import lk.icbt.eduvision.eduvision360.ai.model.AiInteractionType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AiInteractionRepository extends MongoRepository<AiInteraction, String> {

    List<AiInteraction> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<AiInteraction> findByStudentIdAndCourseIdOrderByCreatedAtDesc(String studentId, String courseId);

    long countByStudentIdAndInteractionType(String studentId, AiInteractionType interactionType);

    long countByStudentIdAndCourseIdAndInteractionType(String studentId, String courseId, AiInteractionType interactionType);

    long countByStudentIdAndCourseIdAndCreatedAtAfter(String studentId, String courseId, Instant after);

    long countByStudentIdAndCourseIdAndInteractionTypeAndCreatedAtAfter(
            String studentId,
            String courseId,
            AiInteractionType interactionType,
            Instant after
    );

    Optional<AiInteraction> findTopByStudentIdAndCourseIdOrderByCreatedAtDesc(String studentId, String courseId);
}