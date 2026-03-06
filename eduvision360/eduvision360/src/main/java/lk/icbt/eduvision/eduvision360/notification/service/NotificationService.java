package lk.icbt.eduvision.eduvision360.notification.service;

import lk.icbt.eduvision.eduvision360.notification.dto.NotificationResponse;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.List;

public interface NotificationService {

    void createNotification(
            String userId,
            String title,
            String message,
            NotificationType type,
            String relatedEntityId,
            String relatedEntityType,
            String actionUrl
    );

    // ✅ NEW: cooldown protection (important for engagement frames)
    void createNotificationWithCooldown(
            String userId,
            String title,
            String message,
            NotificationType type,
            String relatedEntityId,
            String relatedEntityType,
            String actionUrl,
            Duration cooldown
    );

    // ✅ NEW: allow priority (optional)
    void createNotification(
            String userId,
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority,
            String relatedEntityId,
            String relatedEntityType,
            String actionUrl
    );

    // ✅ NEW: unified list fetch
    List<NotificationResponse> getMyNotifications(Authentication authentication, boolean unreadOnly, int limit);

    // ✅ kept for backward-compat (your controller can still call these)
    List<NotificationResponse> getMyNotifications(Authentication authentication);

    List<NotificationResponse> getMyUnreadNotifications(Authentication authentication);

    long getMyUnreadCount(Authentication authentication);

    NotificationResponse markAsRead(String notificationId, Authentication authentication);

    void markAllAsRead(Authentication authentication);
}