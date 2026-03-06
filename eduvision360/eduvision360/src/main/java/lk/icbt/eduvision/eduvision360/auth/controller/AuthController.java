package lk.icbt.eduvision.eduvision360.auth.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.auth.dto.*;
import lk.icbt.eduvision.eduvision360.auth.service.AuthService;
import lk.icbt.eduvision.eduvision360.auth.service.FaceAuthService;
import lk.icbt.eduvision.eduvision360.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final FaceAuthService faceAuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(201).body(new ApiResponse(Instant.now(), "OTP sent to email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verify(@Valid @RequestBody VerifyOtpRequest req) {
        authService.verifyOtp(req);
        return ResponseEntity.ok(new ApiResponse(Instant.now(), "Account verified"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(@RequestParam String email) {
        authService.resendRegisterOtp(email);
        return ResponseEntity.ok(new ApiResponse(Instant.now(), "OTP resent"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/face-login")
    public ResponseEntity<AuthResponse> faceLogin(@RequestBody FaceLoginRequest req) {
        AuthResponse res = faceAuthService.faceLogin(req.imageBase64());
        return ResponseEntity.ok(res);
    }


}
