package lk.icbt.eduvision.eduvision360.auth.dto;

public record AuthResponse(
        String token,
        String userId,
        String email,
        String role,
        String fullName
) {}