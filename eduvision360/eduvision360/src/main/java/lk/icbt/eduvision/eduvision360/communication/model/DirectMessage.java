package lk.icbt.eduvision.eduvision360.communication.model;

import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "direct_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "receiver_created_idx", def = "{'receiverId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "sender_created_idx", def = "{'senderId': 1, 'createdAt': -1}")
})
public class DirectMessage {

    @Id
    private String id;

    private String senderId;
    private String senderName;
    private UserRole senderRole;

    private String receiverId;
    private String receiverName;
    private UserRole receiverRole;

    private String subject;
    private String body;

    @Builder.Default
    private boolean read = false;

    private Instant readAt;

    @Builder.Default
    private Instant createdAt = Instant.now();
}