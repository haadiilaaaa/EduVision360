package lk.icbt.eduvision.eduvision360.profile.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.profile.dto.ProfileResponse;
import lk.icbt.eduvision.eduvision360.profile.dto.UpdateProfileRequest;
import lk.icbt.eduvision.eduvision360.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(
                profileService.getMyProfile(authentication.getName())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                profileService.updateMyProfile(authentication.getName(), req)
        );
    }
}