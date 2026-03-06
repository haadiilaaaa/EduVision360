package lk.icbt.eduvision.eduvision360.auth.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;   // MongoDB ObjectId

    private String fullName;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String contactNumber;

    private String passwordHash;

    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant verifiedAt;

    @Indexed(unique = true, sparse = true)
    private String studentCode;

    // -----------------------------
    // Analytics tracking fields
    // -----------------------------

    /**
     * Most recent successful login timestamp.
     */
    private Instant lastLoginAt;

    /**
     * Previous successful login timestamp before the current one.
     * Useful for behavior change analysis.
     */
    private Instant previousLoginAt;

    /**
     * Total successful login count.
     */
    @Builder.Default
    private Long loginCount = 0L;
}