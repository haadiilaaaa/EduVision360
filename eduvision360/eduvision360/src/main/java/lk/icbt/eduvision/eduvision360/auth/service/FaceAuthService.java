package lk.icbt.eduvision.eduvision360.auth.service;

import lk.icbt.eduvision.eduvision360.attendance.client.FaceApiClient;
import lk.icbt.eduvision.eduvision360.attendance.dto.FaceVerificationResponse;
import lk.icbt.eduvision.eduvision360.auth.dto.AuthResponse;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaceAuthService {

    private final FaceApiClient faceApiClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse faceLogin(String imageBase64) {

        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new IllegalArgumentException("Image is required");
        }

        // strip "data:image/jpeg;base64," prefix if present
        String cleaned = imageBase64;
        if (cleaned.startsWith("data:")) {
            int comma = cleaned.indexOf(",");
            if (comma > 0) cleaned = cleaned.substring(comma + 1);
        }

        final byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(cleaned);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 image data");
        }

        FaceVerificationResponse resp = faceApiClient.verify(imageBytes);

        if (resp == null || !resp.isRecognized()) {
            throw new IllegalArgumentException(resp != null ? resp.getMessage() : "Face not recognized");
        }

        String identifier = resp.getStudentId(); // your python returns studentId from filename
        User user = resolveUser(identifier);

        if (user == null) {
            throw new IllegalArgumentException("Recognized user not found: " + identifier);
        }

        String role = user.getRole() != null ? user.getRole().name() : "STUDENT";
        Map<String, Object> claims = Map.of(
                "role", role,
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "userId", user.getId()
        );

        String token = jwtService.generateToken(user.getId(), claims);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                role,
                user.getFullName()
        );
    }

    private User resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;

        if (identifier.contains("@")) {
            return userRepository.findByEmailIgnoreCase(identifier).orElse(null);
        }
        return userRepository.findById(identifier).orElse(null);
    }
}