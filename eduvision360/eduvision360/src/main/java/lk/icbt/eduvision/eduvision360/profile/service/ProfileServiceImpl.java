package lk.icbt.eduvision.eduvision360.profile.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.profile.dto.ProfileResponse;
import lk.icbt.eduvision.eduvision360.profile.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    public ProfileResponse getMyProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toResponse(user);
    }

    @Override
    public ProfileResponse updateMyProfile(String userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFullName(req.getFullName().trim());
        user.setContactNumber(req.getContactNumber() != null ? req.getContactNumber().trim() : null);

        return toResponse(userRepository.save(user));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getContactNumber(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getVerifiedAt(),
                user.getStudentCode()
        );
    }
}