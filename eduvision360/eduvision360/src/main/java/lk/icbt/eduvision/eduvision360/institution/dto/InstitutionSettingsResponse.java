package lk.icbt.eduvision.eduvision360.institution.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InstitutionSettingsResponse {
    private String id;

    private String institutionName;
    private String institutionCode;

    private String contactEmail;
    private String contactNumber;
    private String supportEmail;

    private String address;
    private String websiteUrl;
    private String logoUrl;

    private String academicYear;
    private String currentSemester;

    private String description;
    private String timezone;

    private Instant updatedAt;
    private String updatedByUserId;
    private String updatedByName;
}