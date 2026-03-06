package lk.icbt.eduvision.eduvision360.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Contact number must be exactly 10 digits"
    )
    private String contactNumber;
}