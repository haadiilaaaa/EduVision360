package lk.icbt.eduvision.eduvision360.auth.service;

import lk.icbt.eduvision.eduvision360.auth.dto.LoginRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.RegisterRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.VerifyOtpRequest;
import lk.icbt.eduvision.eduvision360.auth.model.OtpPurpose;
import lk.icbt.eduvision.eduvision360.auth.model.OtpToken;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.model.UserStatus;
import lk.icbt.eduvision.eduvision360.auth.repository.OtpTokenRepository;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 1 - Authentication Service Automated Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private OtpTokenRepository otpRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private EmailSender emailSender;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpiryMinutes", 5L);
    }

    @Test
    @Order(1)
    @DisplayName("ATC-01: Verify OTP should activate a student account when OTP is valid")
    void verifyOtp_shouldActivateStudent_whenOtpIsValid() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("123456");

        User user = new User();
        user.setId("u1");
        user.setEmail("student@test.com");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        OtpToken token = new OtpToken();
        token.setUserId("u1");
        token.setPurpose(OtpPurpose.REGISTER);
        token.setOtpHash("hashed-otp");
        token.setAttempts(0);
        token.setCreatedAt(Instant.now().minusSeconds(30));
        token.setExpiresAt(Instant.now().plusSeconds(120));

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc("u1", OtpPurpose.REGISTER))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);

        authService.verifyOtp(req);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getVerifiedAt());
        assertNotNull(token.getVerifiedAt());

        verify(otpRepo).save(token);
        verify(userRepo).save(user);
    }

    @Test
    @Order(2)
    @DisplayName("ATC-02: Verify OTP should set teacher status to pending approval when OTP is valid")
    void verifyOtp_shouldSetTeacherToPendingApproval_whenOtpIsValid() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("teacher@test.com");
        req.setOtp("123456");

        User user = new User();
        user.setId("u2");
        user.setEmail("teacher@test.com");
        user.setRole(UserRole.TEACHER);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        OtpToken token = new OtpToken();
        token.setUserId("u2");
        token.setPurpose(OtpPurpose.REGISTER);
        token.setOtpHash("hashed-otp");
        token.setAttempts(0);
        token.setCreatedAt(Instant.now().minusSeconds(30));
        token.setExpiresAt(Instant.now().plusSeconds(120));

        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(user));
        when(otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc("u2", OtpPurpose.REGISTER))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);

        authService.verifyOtp(req);

        assertEquals(UserStatus.PENDING_APPROVAL, user.getStatus());
        assertNotNull(user.getVerifiedAt());
        assertNotNull(token.getVerifiedAt());

        verify(otpRepo).save(token);
        verify(userRepo).save(user);
    }

    @Test
    @Order(3)
    @DisplayName("ATC-03: Verify OTP should fail when the OTP is expired")
    void verifyOtp_shouldFail_whenOtpExpired() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("123456");

        User user = new User();
        user.setId("u1");
        user.setEmail("student@test.com");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        OtpToken token = new OtpToken();
        token.setUserId("u1");
        token.setPurpose(OtpPurpose.REGISTER);
        token.setOtpHash("hashed-otp");
        token.setAttempts(0);
        token.setCreatedAt(Instant.now().minusSeconds(300));
        token.setExpiresAt(Instant.now().minusSeconds(1));

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc("u1", OtpPurpose.REGISTER))
                .thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(req)
        );

        assertEquals("OTP expired. Please request a new OTP.", ex.getMessage());
        verify(otpRepo, never()).save(any());
        verify(userRepo, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("ATC-04: Verify OTP should fail and increase attempts when OTP does not match")
    void verifyOtp_shouldFail_whenOtpDoesNotMatch_andIncreaseAttempts() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("999999");

        User user = new User();
        user.setId("u1");
        user.setEmail("student@test.com");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        OtpToken token = new OtpToken();
        token.setUserId("u1");
        token.setPurpose(OtpPurpose.REGISTER);
        token.setOtpHash("hashed-otp");
        token.setAttempts(1);
        token.setCreatedAt(Instant.now().minusSeconds(30));
        token.setExpiresAt(Instant.now().plusSeconds(120));

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc("u1", OtpPurpose.REGISTER))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("999999", "hashed-otp")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(req)
        );

        assertEquals("Invalid OTP", ex.getMessage());
        assertEquals(2, token.getAttempts());
        verify(otpRepo).save(token);
        verify(userRepo, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("ATC-05: Login should return JWT and update login statistics for valid credentials")
    void login_shouldReturnJwt_andUpdateLoginStats_whenCredentialsValid() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("Student@Test.com");
        req.setPassword("Password123!");

        User user = new User();
        user.setId("u1");
        user.setEmail("student@test.com");
        user.setUsername("student1");
        user.setFullName("Test Student");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash("hashed-password");
        user.setLoginCount(0L);

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(eq("u1"), anyMap())).thenReturn("fake-jwt-token");

        String token = authService.login(req);

        assertEquals("fake-jwt-token", token);
        assertNotNull(user.getLastLoginAt());
        assertEquals(1L, user.getLoginCount());
        verify(userRepo).save(user);
        verify(jwtService).generateToken(eq("u1"), argThat((ArgumentMatcher<Map<String, Object>>) claims ->
                "student@test.com".equals(claims.get("email")) &&
                        "Test Student".equals(claims.get("fullName")) &&
                        "STUDENT".equals(claims.get("role")) &&
                        "u1".equals(claims.get("userId"))
        ));
    }

    @Test
    @Order(6)
    @DisplayName("ATC-06: Login should fail when the user account is pending admin approval")
    void login_shouldFail_whenUserPendingApproval() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("teacher@test.com");
        req.setPassword("Password123!");

        User user = new User();
        user.setId("u2");
        user.setEmail("teacher@test.com");
        user.setUsername("teacher1");
        user.setRole(UserRole.TEACHER);
        user.setStatus(UserStatus.PENDING_APPROVAL);
        user.setPasswordHash("hashed-password");

        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> authService.login(req)
        );

        assertEquals("Account pending admin approval", ex.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyMap());
        verify(userRepo, never()).save(any());
    }

    @Test
    @Order(7)
    @DisplayName("ATC-07: Resend registration OTP should delete old OTP and send a new OTP")
    void resendRegisterOtp_shouldDeleteOldOtp_andSendNewOtp() {
        User user = new User();
        user.setId("u1");
        user.setEmail("student@test.com");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(otpGenerator.generate6Digits()).thenReturn("123456");
        when(passwordEncoder.encode("123456")).thenReturn("encoded-otp");

        authService.resendRegisterOtp(" Student@Test.com ");

        verify(otpRepo).deleteByUserIdAndPurposeAndVerifiedAtIsNull("u1", OtpPurpose.REGISTER);
        verify(otpRepo).save(argThat(token ->
                "u1".equals(token.getUserId()) &&
                        token.getPurpose() == OtpPurpose.REGISTER &&
                        "encoded-otp".equals(token.getOtpHash()) &&
                        token.getAttempts() == 0 &&
                        token.getCreatedAt() != null &&
                        token.getExpiresAt() != null
        ));
        verify(emailSender).sendRegistrationOtp("student@test.com", "123456");
    }

    @Test
    @Order(8)
    @DisplayName("ATC-08: Register should create a new teacher account and send OTP")
    void register_shouldCreateNewTeacher_andSendOtp() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("  Test Teacher  ");
        req.setUsername("TeacherOne");
        req.setEmail("Teacher@Test.com");
        req.setContactNumber(" 0771234567 ");
        req.setPassword("Password123!");
        req.setRole(UserRole.TEACHER);

        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.empty());
        when(userRepo.existsByUsername("teacherone")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(otpGenerator.generate6Digits()).thenReturn("654321");
        when(passwordEncoder.encode("654321")).thenReturn("encoded-otp");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId("u100");
            }
            return saved;
        });

        authService.register(req);

        verify(userRepo).save(argThat(user ->
                "u100".equals(user.getId()) &&
                        "Test Teacher".equals(user.getFullName()) &&
                        "teacherone".equals(user.getUsername()) &&
                        "teacher@test.com".equals(user.getEmail()) &&
                        "0771234567".equals(user.getContactNumber()) &&
                        "encoded-password".equals(user.getPasswordHash()) &&
                        user.getRole() == UserRole.TEACHER &&
                        user.getStatus() == UserStatus.PENDING_VERIFICATION &&
                        user.getStudentCode() == null &&
                        user.getCreatedAt() != null
        ));

        verify(otpRepo).save(argThat(token ->
                "u100".equals(token.getUserId()) &&
                        token.getPurpose() == OtpPurpose.REGISTER &&
                        "encoded-otp".equals(token.getOtpHash())
        ));

        verify(emailSender).sendRegistrationOtp("teacher@test.com", "654321");
    }
    @Test
    @Order(9)
    @DisplayName("ATC-09: Register should continue signup for an existing pending-verification account and issue a new OTP")
    void register_shouldContinueSignupForPendingVerificationUser_andIssueNewOtp() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName(" Updated Teacher ");
        req.setUsername("teacherone");
        req.setEmail("teacher@test.com");
        req.setContactNumber(" 0779999999 ");
        req.setPassword("NewPassword123!");
        req.setRole(UserRole.TEACHER);

        User existing = new User();
        existing.setId("u200");
        existing.setFullName("Old Name");
        existing.setUsername("teacherone");
        existing.setEmail("teacher@test.com");
        existing.setContactNumber("0700000000");
        existing.setPasswordHash("old-hash");
        existing.setRole(UserRole.TEACHER);
        existing.setStatus(UserStatus.PENDING_VERIFICATION);

        when(userRepo.findByEmail("teacher@test.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encoded-new-password");
        when(otpGenerator.generate6Digits()).thenReturn("112233");
        when(passwordEncoder.encode("112233")).thenReturn("encoded-otp");

        authService.register(req);

        assertEquals("Updated Teacher", existing.getFullName());
        assertEquals("teacherone", existing.getUsername());
        assertEquals("0779999999", existing.getContactNumber());
        assertEquals("encoded-new-password", existing.getPasswordHash());
        assertEquals(UserRole.TEACHER, existing.getRole());

        verify(userRepo).save(existing);
        verify(otpRepo).deleteByUserIdAndPurposeAndVerifiedAtIsNull("u200", OtpPurpose.REGISTER);
        verify(otpRepo).save(argThat(token ->
                "u200".equals(token.getUserId()) &&
                        token.getPurpose() == OtpPurpose.REGISTER &&
                        "encoded-otp".equals(token.getOtpHash())
        ));
        verify(emailSender).sendRegistrationOtp("teacher@test.com", "112233");
    }

    @Test
    @Order(10)
    @DisplayName("ATC-10: Register should fail when email already belongs to an active account")
    void register_shouldFail_whenEmailBelongsToActiveAccount() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setUsername("newuser");
        req.setEmail("active@test.com");
        req.setContactNumber("0771234567");
        req.setPassword("Password123!");
        req.setRole(UserRole.STUDENT);

        User existing = new User();
        existing.setId("u201");
        existing.setEmail("active@test.com");
        existing.setUsername("existinguser");
        existing.setStatus(UserStatus.ACTIVE);

        when(userRepo.findByEmail("active@test.com")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(req)
        );

        assertEquals("Email already in use", ex.getMessage());
        verify(userRepo, never()).save(any());
        verify(otpRepo, never()).save(any());
        verify(emailSender, never()).sendRegistrationOtp(anyString(), anyString());
    }

    @Test
    @Order(11)
    @DisplayName("ATC-11: Register should fail when username already exists for a new account")
    void register_shouldFail_whenUsernameAlreadyExistsForNewAccount() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test Student");
        req.setUsername("student1");
        req.setEmail("student-new@test.com");
        req.setContactNumber("0771234567");
        req.setPassword("Password123!");
        req.setRole(UserRole.STUDENT);

        when(userRepo.findByEmail("student-new@test.com")).thenReturn(Optional.empty());
        when(userRepo.existsByUsername("student1")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(req)
        );

        assertEquals("Username already in use", ex.getMessage());
        verify(userRepo, never()).save(any());
        verify(otpRepo, never()).save(any());
        verify(emailSender, never()).sendRegistrationOtp(anyString(), anyString());
    }

    @Test
    @Order(12)
    @DisplayName("ATC-12: Resend registration OTP should fail when the user is already verified")
    void resendRegisterOtp_shouldFail_whenUserAlreadyActive() {
        User user = new User();
        user.setId("u202");
        user.setEmail("active@test.com");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepo.findByEmail("active@test.com")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.resendRegisterOtp("active@test.com")
        );

        assertEquals("User already verified", ex.getMessage());
        verify(otpRepo, never()).deleteByUserIdAndPurposeAndVerifiedAtIsNull(anyString(), any());
        verify(otpRepo, never()).save(any());
        verify(emailSender, never()).sendRegistrationOtp(anyString(), anyString());
    }

    @Test
    @Order(13)
    @DisplayName("ATC-13: Verify OTP should fail when no OTP record exists")
    void verifyOtp_shouldFail_whenNoOtpRecordExists() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("123456");

        User user = new User();
        user.setId("u203");
        user.setEmail("student@test.com");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc("u203", OtpPurpose.REGISTER))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(req)
        );

        assertEquals("No OTP found. Please request a new OTP.", ex.getMessage());
        verify(otpRepo, never()).save(any());
        verify(userRepo, never()).save(any());
    }

    @Test
    @Order(14)
    @DisplayName("ATC-14: Verify OTP should fail after maximum allowed attempts")
    void verifyOtp_shouldFail_whenMaximumAttemptsReached() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("123456");

        User user = new User();
        user.setId("u204");
        user.setEmail("student@test.com");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        OtpToken token = new OtpToken();
        token.setUserId("u204");
        token.setPurpose(OtpPurpose.REGISTER);
        token.setOtpHash("hashed-otp");
        token.setAttempts(5);
        token.setCreatedAt(Instant.now().minusSeconds(30));
        token.setExpiresAt(Instant.now().plusSeconds(120));

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(otpRepo.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc("u204", OtpPurpose.REGISTER))
                .thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(req)
        );

        assertEquals("Too many attempts. Please request a new OTP.", ex.getMessage());
        verify(otpRepo, never()).save(any());
        verify(userRepo, never()).save(any());
    }

    @Test
    @Order(15)
    @DisplayName("ATC-15: Login should fail when the password is incorrect")
    void login_shouldFail_whenPasswordIsIncorrect() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("student@test.com");
        req.setPassword("WrongPassword123!");

        User user = new User();
        user.setId("u205");
        user.setEmail("student@test.com");
        user.setUsername("student1");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash("hashed-password");

        when(userRepo.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123!", "hashed-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(req)
        );

        assertEquals("Invalid credentials", ex.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyMap());
        verify(userRepo, never()).save(any());
    }

    @Test
    @Order(16)
    @DisplayName("ATC-16: Login should fail when the account is disabled")
    void login_shouldFail_whenAccountIsDisabled() {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("disabled@test.com");
        req.setPassword("Password123!");

        User user = new User();
        user.setId("u206");
        user.setEmail("disabled@test.com");
        user.setUsername("disableduser");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.DISABLED);
        user.setPasswordHash("hashed-password");

        when(userRepo.findByEmail("disabled@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> authService.login(req)
        );

        assertEquals("Account disabled by administrator", ex.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyMap());
        verify(userRepo, never()).save(any());
    }
}