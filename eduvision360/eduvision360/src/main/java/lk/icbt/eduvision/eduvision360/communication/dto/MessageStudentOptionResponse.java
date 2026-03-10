package lk.icbt.eduvision.eduvision360.communication.dto;

public record MessageStudentOptionResponse(
        String id,
        String fullName,
        String email,
        String studentCode
) {}