package lk.icbt.eduvision.eduvision360.admin.dto;

import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.model.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AdminUserResponse {
    private String id;
    private String fullName;
    private String username;
    private String email;
    private String contactNumber;
    private UserRole role;
    private UserStatus status;
    private String studentCode;
    private Instant createdAt;
    private Instant verifiedAt;
}