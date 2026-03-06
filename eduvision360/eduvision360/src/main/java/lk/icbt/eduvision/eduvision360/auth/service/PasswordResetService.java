package lk.icbt.eduvision.eduvision360.auth.service;

import lk.icbt.eduvision.eduvision360.auth.model.PasswordResetToken;
import lk.icbt.eduvision.eduvision360.auth.repository.PasswordResetTokenRepository;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    // 🔹 Step 1: Send reset link
    public void sendResetLink(String email) {

        if (!userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email not registered");
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        tokenRepo.save(resetToken);

        String resetLink =
                "http://localhost:3000/reset-password?token=" + token;

        emailSender.sendPasswordResetLink(email, resetLink);
    }

    // 🔹 Step 2: Reset password
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepo
                .findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        userRepo.findByEmail(resetToken.getEmail()).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepo.save(user);
        });

        resetToken.setUsed(true);
        tokenRepo.save(resetToken);
    }
}

