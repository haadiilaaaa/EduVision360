package lk.icbt.eduvision.eduvision360.profile.service;

import lk.icbt.eduvision.eduvision360.profile.dto.ProfileResponse;
import lk.icbt.eduvision.eduvision360.profile.dto.UpdateProfileRequest;

public interface ProfileService {

    ProfileResponse getMyProfile(String userId);

    ProfileResponse updateMyProfile(String userId, UpdateProfileRequest req);
}