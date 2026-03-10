package lk.icbt.eduvision.eduvision360.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.icbt.eduvision.eduvision360.auth.dto.LoginRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.RegisterRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.VerifyOtpRequest;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.service.AuthService;
import lk.icbt.eduvision.eduvision360.auth.service.FaceAuthService;
import lk.icbt.eduvision.eduvision360.common.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 3 - Authentication Controller API Automated Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private FaceAuthService faceAuthService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @Order(17)
    @DisplayName("ATC-17: Login endpoint should return token for valid credentials")
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("student@test.com");
        req.setPassword("Password123!");

        when(authService.login(any(LoginRequest.class))).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    @Order(18)
    @DisplayName("ATC-18: Login endpoint should reject invalid credentials")
    void login_shouldRejectInvalidCredentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("student@test.com");
        req.setPassword("WrongPassword123!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @Order(19)
    @DisplayName("ATC-19: Register endpoint should accept valid teacher registration")
    void register_shouldAcceptValidTeacherRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test Teacher");
        req.setUsername("teacherone");
        req.setEmail("teacher@test.com");
        req.setContactNumber("0771234567");
        req.setPassword("Password123!");
        req.setRole(UserRole.TEACHER);

        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("OTP sent to email"));
    }

    @Test
    @Order(20)
    @DisplayName("ATC-20: Register endpoint should reject duplicate email")
    void register_shouldRejectDuplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test Teacher");
        req.setUsername("teacherone");
        req.setEmail("teacher@test.com");
        req.setContactNumber("0771234567");
        req.setPassword("Password123!");
        req.setRole(UserRole.TEACHER);

        doThrow(new IllegalArgumentException("Email already in use"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Email already in use"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    @Order(21)
    @DisplayName("ATC-21: Verify OTP endpoint should accept valid OTP")
    void verifyOtp_shouldAcceptValidOtp() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("123456");

        doNothing().when(authService).verifyOtp(any(VerifyOtpRequest.class));

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified"));
    }

    @Test
    @Order(22)
    @DisplayName("ATC-22: Verify OTP endpoint should reject expired OTP")
    void verifyOtp_shouldRejectExpiredOtp() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("student@test.com");
        req.setOtp("123456");

        doThrow(new IllegalArgumentException("OTP expired. Please request a new OTP."))
                .when(authService).verifyOtp(any(VerifyOtpRequest.class));

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("OTP expired. Please request a new OTP."))
                .andExpect(jsonPath("$.path").value("/api/auth/verify-otp"));
    }

    @Test
    @Order(23)
    @DisplayName("ATC-23: Resend OTP endpoint should accept a valid pending-verification user")
    void resendOtp_shouldAcceptPendingVerificationUser() throws Exception {
        doNothing().when(authService).resendRegisterOtp("student@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                        .param("email", "student@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP resent"));
    }

    @Test
    @Order(24)
    @DisplayName("ATC-24: Resend OTP endpoint should reject already verified user")
    void resendOtp_shouldRejectAlreadyVerifiedUser() throws Exception {
        doThrow(new IllegalArgumentException("User already verified"))
                .when(authService).resendRegisterOtp("student@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                        .param("email", "student@test.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("User already verified"))
                .andExpect(jsonPath("$.path").value("/api/auth/resend-otp"));
    }
}