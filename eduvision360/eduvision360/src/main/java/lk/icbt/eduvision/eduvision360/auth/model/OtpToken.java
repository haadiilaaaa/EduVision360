package lk.icbt.eduvision.eduvision360.auth.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "otp_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    @Id
    private String id;

    private String userId;
    private OtpPurpose purpose;

    private String otpHash;     // ✅ store hash instead of otp
    private Instant createdAt;
    private Instant expiresAt;
    private Instant verifiedAt;

    private int attempts;
}
