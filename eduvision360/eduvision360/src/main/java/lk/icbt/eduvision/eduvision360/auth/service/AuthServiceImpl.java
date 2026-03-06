package lk.icbt.eduvision.eduvision360.auth.service;

import lk.icbt.eduvision.eduvision360.auth.dto.LoginRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.RegisterRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.VerifyOtpRequest;
import lk.icbt.eduvision.eduvision360.auth.model.*;
import lk.icbt.eduvision.eduvision360.auth.repository.OtpTokenRepository;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepo;
    private final OtpTokenRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;
    private final EmailSender emailSender;
    private final JwtService jwtService;

    @Value("${app.otp.expiry.minutes:5}")
    private long otpExpiryMinutes;

    @Override
    public void register(RegisterRequest req) {

        String email = normalizeEmail(req.getEmail());
        String username = normalizeUsername(req.getUsername());
        String contact = normalizeContact(req.getContactNumber());
        UserRole role = req.getRole();

        // ✅ if email exists:
        // - PENDING_VERIFICATION => resend OTP (continue signup)
        // - PENDING_APPROVAL / DISABLED / ACTIVE => block with proper message
        var existingOpt = userRepo.findByEmail(email);
        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            if (existing.getStatus() == UserStatus.PENDING_VERIFICATION) {

                // allow username change only if new username isn't taken
                if (!existing.getUsername().equals(username) && userRepo.existsByUsername(username)) {
                    throw new IllegalArgumentException("Username already in use");
                }

                existing.setFullName(req.getFullName().trim());
                existing.setUsername(username);
                existing.setContactNumber(contact);
                existing.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                existing.setRole(role); // keep or remove if you want role locked

                userRepo.save(existing);

                otpRepo.deleteByUserIdAndPurposeAndVerifiedAtIsNull(existing.getId(), OtpPurpose.REGISTER);
                issueRegisterOtp(existing);
                return;
            }

            if (existing.getStatus() == UserStatus.PENDING_APPROVAL) {
                throw new IllegalStateException("Account pending admin approval");
            }

            if (existing.getStatus() == UserStatus.DISABLED) {
                throw new IllegalStateException("Account disabled by administrator");
            }

            // ACTIVE
            throw new IllegalArgumentException("Email already in use");
        }

        // New email → username must be unique
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
        }

        // student code if student
        String studentCode = null;
        if (role == UserRole.STUDENT) {
            studentCode = generateNextStudentCode();
        }

        User newUser = User.builder()
                .fullName(req.getFullName().trim())
                .username(username)
                .email(email)
                .contactNumber(contact)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .status(UserStatus.PENDING_VERIFICATION)
                .studentCode(studentCode)
                .createdAt(Instant.now())
                .build();

        userRepo.save(newUser);
        issueRegisterOtp(newUser);
    }

    // ✅ safer than count-only (handles deletes + uniqueness)
    private String generateNextStudentCode() {
        long base = userRepo.countByRole(UserRole.STUDENT) + 1;
        for (int i = 0; i < 200; i++) {
            String code = "ST" + String.format("%03d", base + i);
            if (!userRepo.existsByStudentCode(code)) return code;
        }
        return "ST" + (System.currentTimeMillis() % 1_000_000);
    }

    @Override
    public void verifyOtp(VerifyOtpRequest req) {
        String email = normalizeEmail(req.getEmail());

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) return;

        OtpToken token = otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(
                        user.getId(), OtpPurpose.REGISTER)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found. Please request a new OTP."));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP expired. Please request a new OTP.");
        }

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("Too many attempts. Please request a new OTP.");
        }

        boolean matches = passwordEncoder.matches(req.getOtp(), token.getOtpHash());
        if (!matches) {
            token.setAttempts(token.getAttempts() + 1);
            otpRepo.save(token);
            throw new IllegalArgumentException("Invalid OTP");
        }

        token.setVerifiedAt(Instant.now());
        otpRepo.save(token);

        if (user.getRole() == UserRole.TEACHER) user.setStatus(UserStatus.PENDING_APPROVAL);
        else user.setStatus(UserStatus.ACTIVE);

        user.setVerifiedAt(Instant.now());
        userRepo.save(user);
    }

    @Override
    public void resendRegisterOtp(String email) {
        User user = userRepo.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User already verified");
        }

        otpRepo.deleteByUserIdAndPurposeAndVerifiedAtIsNull(user.getId(), OtpPurpose.REGISTER);
        issueRegisterOtp(user);
    }

    @Override
    public String login(LoginRequest req) {

        String identifier = req.getIdentifier().trim().toLowerCase();

        User user = userRepo.findByEmail(identifier)
                .or(() -> userRepo.findByUsername(identifier))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Account pending admin approval");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new IllegalStateException("Account disabled by administrator");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Account not verified");
        }

        updateSuccessfulLoginStats(user);

        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),     // ✅ add this
                "role", user.getRole().name(),
                "userId", user.getId()              // (optional, handy for UI)
        );

        return jwtService.generateToken(user.getId(), claims);
    }

    private void updateSuccessfulLoginStats(User user) {
        Instant now = Instant.now();
        user.setPreviousLoginAt(user.getLastLoginAt());
        user.setLastLoginAt(now);

        Long currentCount = user.getLoginCount() == null ? 0L : user.getLoginCount();
        user.setLoginCount(currentCount + 1);

        userRepo.save(user);
    }

    private void issueRegisterOtp(User user) {
        String otp = otpGenerator.generate6Digits();

        Instant now = Instant.now();
        OtpToken token = OtpToken.builder()
                .userId(user.getId())
                .purpose(OtpPurpose.REGISTER)
                .otpHash(passwordEncoder.encode(otp))
                .createdAt(now)
                .expiresAt(now.plus(otpExpiryMinutes, ChronoUnit.MINUTES))
                .attempts(0)
                .build();

        otpRepo.save(token);

        // If email sending fails, you'll now see the real error in logs (GlobalExceptionHandler logs it)
        emailSender.sendRegistrationOtp(user.getEmail(), otp);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }

    private String normalizeContact(String contact) {
        return contact == null ? null : contact.trim();
    }
}