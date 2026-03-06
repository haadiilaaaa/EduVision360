package lk.icbt.eduvision.eduvision360.feedback.dto;

import jakarta.validation.constraints.NotNull;
import lk.icbt.eduvision.eduvision360.feedback.model.FeedbackStatus;

public record UpdateFeedbackStatusRequest(
        @NotNull(message = "Status is required")
        FeedbackStatus status,

        String responseComment
) {}