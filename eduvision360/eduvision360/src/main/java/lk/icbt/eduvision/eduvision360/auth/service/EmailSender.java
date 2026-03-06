package lk.icbt.eduvision.eduvision360.auth.service;

public interface EmailSender {

    // 🔐 Registration
    void sendRegistrationOtp(String email, String otp);

    // 🔁 Password reset
    void sendPasswordResetLink(String email, String resetLink);

    // 👩‍🏫 Admin decisions
    void sendTeacherApprovedEmail(String email);
    void sendTeacherRejectedEmail(String email);
}
