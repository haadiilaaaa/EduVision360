package lk.icbt.eduvision.eduvision360.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AskAiRequest(
        @NotBlank(message = "Course ID is required")
        String courseId,

        @NotBlank(message = "Question is required")
        String question,

        String contextText,

        // NEW (optional)
        String materialId
) {
}