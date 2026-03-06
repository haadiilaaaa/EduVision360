package lk.icbt.eduvision.eduvision360.ai.dto;

import lk.icbt.eduvision.eduvision360.ai.model.AiInteractionType;
import lk.icbt.eduvision.eduvision360.material.model.MaterialType;

import java.time.Instant;
import java.util.List;

public record AiQuizResponse(
        String interactionId,
        String courseId,
        String courseCode,
        String courseTitle,
        AiInteractionType interactionType,
        String promptText,
        List<QuizQuestionDto> questions,
        Instant createdAt,

        // NEW (optional metadata)
        String materialId,
        String materialTitle,
        MaterialType materialType,
        Integer questionCount,
        Integer sourceTextChars,
        Long aiLatencyMs
) {
}