package lk.icbt.eduvision.eduvision360.institution.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.institution.dto.InstitutionSettingsRequest;
import lk.icbt.eduvision.eduvision360.institution.dto.InstitutionSettingsResponse;
import lk.icbt.eduvision.eduvision360.institution.model.InstitutionSettings;
import lk.icbt.eduvision.eduvision360.institution.repository.InstitutionSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InstitutionSettingsServiceImpl implements InstitutionSettingsService {

    private static final String PRIMARY_ID = "PRIMARY";

    private final InstitutionSettingsRepository institutionSettingsRepository;
    private final UserRepository userRepository;

    @Override
    public InstitutionSettingsResponse getSettings() {
        InstitutionSettings settings = institutionSettingsRepository.findById(PRIMARY_ID)
                .orElseGet(this::buildDefaultSettings);

        if (!institutionSettingsRepository.existsById(PRIMARY_ID)) {
            institutionSettingsRepository.save(settings);
        }

        return mapToResponse(settings);
    }

    @Override
    public InstitutionSettingsResponse updateSettings(InstitutionSettingsRequest request, String adminUserId) {
        if (request.getInstitutionName() == null || request.getInstitutionName().isBlank()) {
            throw new IllegalArgumentException("Institution name is required");
        }

        if ((request.getContactEmail() == null || request.getContactEmail().isBlank())
                && (request.getSupportEmail() == null || request.getSupportEmail().isBlank())) {
            throw new IllegalArgumentException("Either contact email or support email is required");
        }

        InstitutionSettings settings = institutionSettingsRepository.findById(PRIMARY_ID)
                .orElseGet(this::buildDefaultSettings);

        User admin = userRepository.findById(adminUserId).orElse(null);

        settings.setId(PRIMARY_ID);
        settings.setInstitutionName(trim(request.getInstitutionName()));
        settings.setInstitutionCode(trim(request.getInstitutionCode()));

        settings.setContactEmail(trim(request.getContactEmail()));
        settings.setContactNumber(trim(request.getContactNumber()));
        settings.setSupportEmail(trim(request.getSupportEmail()));

        settings.setAddress(trim(request.getAddress()));
        settings.setWebsiteUrl(trim(request.getWebsiteUrl()));
        settings.setLogoUrl(trim(request.getLogoUrl()));

        settings.setAcademicYear(trim(request.getAcademicYear()));
        settings.setCurrentSemester(trim(request.getCurrentSemester()));

        settings.setDescription(trim(request.getDescription()));
        settings.setTimezone(trim(request.getTimezone()));

        settings.setUpdatedAt(Instant.now());
        settings.setUpdatedByUserId(adminUserId);
        settings.setUpdatedByName(admin != null ? admin.getFullName() : null);

        InstitutionSettings saved = institutionSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private InstitutionSettings buildDefaultSettings() {
        return InstitutionSettings.builder()
                .id(PRIMARY_ID)
                .institutionName("EduVision360 Institution")
                .institutionCode("EDUVISION")
                .contactEmail("")
                .contactNumber("")
                .supportEmail("")
                .address("")
                .websiteUrl("")
                .logoUrl("")
                .academicYear("")
                .currentSemester("")
                .description("")
                .timezone("Asia/Colombo")
                .updatedAt(Instant.now())
                .build();
    }

    private InstitutionSettingsResponse mapToResponse(InstitutionSettings settings) {
        return InstitutionSettingsResponse.builder()
                .id(settings.getId())
                .institutionName(settings.getInstitutionName())
                .institutionCode(settings.getInstitutionCode())
                .contactEmail(settings.getContactEmail())
                .contactNumber(settings.getContactNumber())
                .supportEmail(settings.getSupportEmail())
                .address(settings.getAddress())
                .websiteUrl(settings.getWebsiteUrl())
                .logoUrl(settings.getLogoUrl())
                .academicYear(settings.getAcademicYear())
                .currentSemester(settings.getCurrentSemester())
                .description(settings.getDescription())
                .timezone(settings.getTimezone())
                .updatedAt(settings.getUpdatedAt())
                .updatedByUserId(settings.getUpdatedByUserId())
                .updatedByName(settings.getUpdatedByName())
                .build();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}