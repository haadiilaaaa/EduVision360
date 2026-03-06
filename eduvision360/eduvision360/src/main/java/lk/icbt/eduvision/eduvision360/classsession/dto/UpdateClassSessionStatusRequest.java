package lk.icbt.eduvision.eduvision360.classsession.dto;

import jakarta.validation.constraints.NotNull;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;

public record UpdateClassSessionStatusRequest(
        @NotNull(message = "Status is required")
        ClassSessionStatus status
) {}