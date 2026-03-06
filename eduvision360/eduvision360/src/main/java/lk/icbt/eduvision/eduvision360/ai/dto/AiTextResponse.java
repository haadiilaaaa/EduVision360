package lk.icbt.eduvision.eduvision360.ai.dto;

import lk.icbt.eduvision.eduvision360.ai.model.AiInteractionType;
import lk.icbt.eduvision.eduvision360.material.model.MaterialType;

import java.time.Instant;

public record AiTextResponse(
        String interactionId,
        String courseId,
        String courseCode,
        String courseTitle,
        AiInteractionType interactionType,
        String promptText,
        String responseText,
        Instant createdAt,

        // NEW (optional metadata)
        String materialId,
        String materialTitle,
        MaterialType materialType,
        Boolean contextAttached,
        Boolean contextTruncated,
        Integer promptChars,
        Integer contextChars,
        Integer responseChars,
        Long aiLatencyMs
) {
}