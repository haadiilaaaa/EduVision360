package lk.icbt.eduvision.eduvision360.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.icbt.eduvision.eduvision360.feedback.model.FeedbackCategory;

public record CreateFeedbackRequest(
        @NotBlank(message = "Course ID is required")
        String courseId,

        String sessionId,

        @NotNull(message = "Category is required")
        FeedbackCategory category,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @NotBlank(message = "Comment is required")
        String comment
) {}