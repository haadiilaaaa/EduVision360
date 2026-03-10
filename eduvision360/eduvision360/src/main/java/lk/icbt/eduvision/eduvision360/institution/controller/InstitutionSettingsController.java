package lk.icbt.eduvision.eduvision360.institution.controller;

import lk.icbt.eduvision.eduvision360.institution.dto.InstitutionSettingsRequest;
import lk.icbt.eduvision.eduvision360.institution.dto.InstitutionSettingsResponse;
import lk.icbt.eduvision.eduvision360.institution.service.InstitutionSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/institution-settings")
@RequiredArgsConstructor
public class InstitutionSettingsController {

    private final InstitutionSettingsService institutionSettingsService;

    @GetMapping
    public InstitutionSettingsResponse getSettings() {
        return institutionSettingsService.getSettings();
    }

    @PutMapping
    public InstitutionSettingsResponse updateSettings(
            @RequestBody InstitutionSettingsRequest request,
            Authentication authentication
    ) {
        return institutionSettingsService.updateSettings(request, authentication.getName());
    }
}