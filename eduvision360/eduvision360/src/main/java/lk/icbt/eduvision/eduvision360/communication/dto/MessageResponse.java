package lk.icbt.eduvision.eduvision360.communication.dto;

import lk.icbt.eduvision.eduvision360.auth.model.UserRole;

import java.time.Instant;

public record MessageResponse(
        String id,
        String senderId,
        String senderName,
        UserRole senderRole,
        String receiverId,
        String receiverName,
        UserRole receiverRole,
        String subject,
        String body,
        boolean read,
        Instant readAt,
        Instant createdAt
) {}