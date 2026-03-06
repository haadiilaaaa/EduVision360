package lk.icbt.eduvision.eduvision360.profile.dto;

import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.model.UserStatus;

import java.time.Instant;

public record ProfileResponse(
        String id,
        String fullName,
        String username,
        String email,
        String contactNumber,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant verifiedAt,
        String studentCode
) {
}