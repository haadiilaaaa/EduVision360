package lk.icbt.eduvision.eduvision360.notification.dto;

import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String title,
        String message,
        NotificationType type,
        NotificationPriority priority,
        boolean read,
        Instant readAt,
        String relatedEntityId,
        String relatedEntityType,
        String actionUrl,
        Instant createdAt
) {}