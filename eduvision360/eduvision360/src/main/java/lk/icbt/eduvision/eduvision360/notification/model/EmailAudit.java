package lk.icbt.eduvision.eduvision360.notification.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "email_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAudit {

    @Id
    private String id;

    private String toEmail;
    private String subject;
    private String body;

    // optional metadata (useful for evidence + linking)
    private String type;              // e.g., DROPOUT_HIGH_RISK, ATTENDANCE_MISSED
    private String relatedEntityId;   // e.g., predictionId / sessionId
    private String relatedEntityType; // e.g., DROPOUT_PREDICTION / CLASS_SESSION

    // PENDING / SENT / FAILED / SKIPPED
    private String status;

    private String errorMessage;

    private Instant createdAt;
    private Instant sentAt;
}