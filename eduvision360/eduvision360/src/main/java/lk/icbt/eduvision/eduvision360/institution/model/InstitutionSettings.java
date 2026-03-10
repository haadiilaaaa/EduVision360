package lk.icbt.eduvision.eduvision360.institution.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "institution_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionSettings {

    @Id
    @Builder.Default
    private String id = "PRIMARY";

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

    @Builder.Default
    private Instant updatedAt = Instant.now();

    private String updatedByUserId;
    private String updatedByName;
}