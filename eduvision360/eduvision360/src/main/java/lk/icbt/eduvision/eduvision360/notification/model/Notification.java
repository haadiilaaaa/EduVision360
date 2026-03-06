package lk.icbt.eduvision.eduvision360.notification.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        // Fast list view: userId + createdAt desc
        @CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}"),
        // Fast unread badge: userId + read + createdAt desc
        @CompoundIndex(name = "user_read_created_idx", def = "{'userId': 1, 'read': 1, 'createdAt': -1}")
})
public class Notification {

    @Id
    private String id;

    @Indexed
    private String userId; // usually authentication.getName() (email)

    private String title;
    private String message;

    private NotificationType type;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @Builder.Default
    private boolean read = false;

    private Instant readAt;

    private String relatedEntityId;
    private String relatedEntityType;

    private String actionUrl;

    @Builder.Default
    private Instant createdAt = Instant.now();
}