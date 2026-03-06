package lk.icbt.eduvision.eduvision360.auth.repository;

import lk.icbt.eduvision.eduvision360.auth.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
}
