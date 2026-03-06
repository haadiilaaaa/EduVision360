package lk.icbt.eduvision.eduvision360.auth.dto;

public record LoginResponse(
        String tokenType,
        String token,
        String role,
        String username,
        String email
) {}
