package lk.icbt.eduvision.eduvision360.notification.repository;

import lk.icbt.eduvision.eduvision360.notification.model.Notification;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    // ✅ same as before but pageable so you can limit
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

    // ✅ keep count
    long countByUserIdAndReadFalse(String userId);

    // ✅ safer read update (no cross-user access)
    Optional<Notification> findByIdAndUserId(String id, String userId);

    // ✅ used for "cooldown" to prevent spam (engagement etc.)
    boolean existsByUserIdAndTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtAfter(
            String userId,
            NotificationType type,
            String relatedEntityType,
            String relatedEntityId,
            Instant createdAtAfter
    );
}