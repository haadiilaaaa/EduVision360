package lk.icbt.eduvision.eduvision360.auth.model;

public enum UserStatus {
    PENDING_VERIFICATION, // OTP not verified
    PENDING_APPROVAL,     // OTP verified, waiting for admin
    ACTIVE,               // Can login
    DISABLED              // Blocked
}

