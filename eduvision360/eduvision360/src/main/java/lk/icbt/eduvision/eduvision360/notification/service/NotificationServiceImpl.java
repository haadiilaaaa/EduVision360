package lk.icbt.eduvision.eduvision360.notification.service;

import lk.icbt.eduvision.eduvision360.notification.dto.NotificationResponse;
import lk.icbt.eduvision.eduvision360.notification.model.Notification;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void createNotification(
            String userId,
            String title,
            String message,
            NotificationType type,
            String relatedEntityId,
            String relatedEntityType,
            String actionUrl
    ) {
        createNotification(userId, title, message, type, NotificationPriority.MEDIUM, relatedEntityId, relatedEntityType, actionUrl);
    }

    @Override
    public void createNotification(
            String userId,
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority,
            String relatedEntityId,
            String relatedEntityType,
            String actionUrl
    ) {
        if (userId == null || userId.isBlank()) return;

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority != null ? priority : NotificationPriority.MEDIUM)
                .read(false)
                .readAt(null)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .actionUrl(actionUrl)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void createNotificationWithCooldown(
            String userId,
            String title,
            String message,
            NotificationType type,
            String relatedEntityId,
            String relatedEntityType,
            String actionUrl,
            Duration cooldown
    ) {
        if (userId == null || userId.isBlank()) return;
        if (cooldown == null || cooldown.isZero() || cooldown.isNegative()) {
            createNotification(userId, title, message, type, relatedEntityId, relatedEntityType, actionUrl);
            return;
        }

        Instant since = Instant.now().minus(cooldown);

        boolean exists = notificationRepository
                .existsByUserIdAndTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtAfter(
                        userId,
                        type,
                        relatedEntityType,
                        relatedEntityId,
                        since
                );

        if (!exists) {
            createNotification(userId, title, message, type, relatedEntityId, relatedEntityType, actionUrl);
        }
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        return getMyNotifications(authentication, false, 50);
    }

    @Override
    public List<NotificationResponse> getMyUnreadNotifications(Authentication authentication) {
        return getMyNotifications(authentication, true, 50);
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Authentication authentication, boolean unreadOnly, int limit) {
        String userId = authentication.getName();
        int safeLimit = Math.min(Math.max(limit, 1), 200); // 1..200

        var pageable = PageRequest.of(0, safeLimit);

        List<Notification> list = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public long getMyUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public NotificationResponse markAsRead(String notificationId, Authentication authentication) {
        String userId = authentication.getName();

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
        }

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Override
    public void markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();

        // only mark a limited batch to avoid huge updates
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(
                userId, PageRequest.of(0, 500)
        );

        if (unread.isEmpty()) return;

        Instant now = Instant.now();
        unread.forEach(n -> {
            n.setRead(true);
            if (n.getReadAt() == null) n.setReadAt(now);
        });

        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getPriority(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.getActionUrl(),
                notification.getCreatedAt()
        );
    }
}