package lk.icbt.eduvision.eduvision360.institution.service;

import lk.icbt.eduvision.eduvision360.institution.dto.InstitutionSettingsRequest;
import lk.icbt.eduvision.eduvision360.institution.dto.InstitutionSettingsResponse;

public interface InstitutionSettingsService {
    InstitutionSettingsResponse getSettings();
    InstitutionSettingsResponse updateSettings(InstitutionSettingsRequest request, String adminUserId);
}