package lk.icbt.eduvision.eduvision360.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Contact number must be exactly 10 digits"
    )
    private String contactNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;
}
