package lk.icbt.eduvision.eduvision360.common.api;

import java.time.Instant;

public record ApiResponse(
        Instant timestamp,
        String message
) {}
