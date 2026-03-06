package lk.icbt.eduvision.eduvision360.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateSummaryRequest(
        @NotBlank(message = "Course ID is required")
        String courseId,

        @NotBlank(message = "Topic is required")
        String topic,

        @NotBlank(message = "Source text is required")
        String sourceText,

        // NEW (optional)
        String materialId
) {
}