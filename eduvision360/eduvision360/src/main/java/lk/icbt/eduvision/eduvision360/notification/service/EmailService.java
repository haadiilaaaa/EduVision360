package lk.icbt.eduvision.eduvision360.notification.service;

public interface EmailService {

    // Keep your existing signature (no breaking for current calls)
    void sendEmail(String to, String subject, String body);

    // New overload with metadata (use this for dropout/session/attendance flows)
    void sendEmail(
            String to,
            String subject,
            String body,
            String type,
            String relatedEntityId,
            String relatedEntityType
    );
}