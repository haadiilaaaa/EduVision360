package lk.icbt.eduvision.eduvision360.auth.repository;

import lk.icbt.eduvision.eduvision360.auth.model.OtpPurpose;
import lk.icbt.eduvision.eduvision360.auth.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    Optional<OtpToken> findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(
            String userId, OtpPurpose purpose
    );

    void deleteByUserIdAndPurposeAndVerifiedAtIsNull(String userId, OtpPurpose purpose);
}
