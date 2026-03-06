package lk.icbt.eduvision.eduvision360.auth.controller;

import lk.icbt.eduvision.eduvision360.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // 🔹 Step 1: Request reset link
    @PostMapping("/request")
    public ResponseEntity<?> requestResetLink(@RequestParam String email) {
        passwordResetService.sendResetLink(email);
        return ResponseEntity.ok(
                Map.of("message", "Password reset link sent to email")
        );
    }

    // 🔹 Step 2: Reset password using token
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok(
                Map.of("message", "Password reset successful")
        );
    }
}
