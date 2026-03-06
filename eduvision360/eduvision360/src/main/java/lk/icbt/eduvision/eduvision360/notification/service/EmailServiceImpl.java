package lk.icbt.eduvision.eduvision360.notification.service;

import lk.icbt.eduvision.eduvision360.notification.model.EmailAudit;
import lk.icbt.eduvision.eduvision360.notification.repository.EmailAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailAuditRepository emailAuditRepository;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Override
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, null, null, null);
    }

    @Override
    public void sendEmail(
            String to,
            String subject,
            String body,
            String type,
            String relatedEntityId,
            String relatedEntityType
    ) {
        if (to == null || to.isBlank()) return;

        EmailAudit audit = EmailAudit.builder()
                .toEmail(to)
                .subject(subject)
                .body(body)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        try {
            // If disabled, don't attempt SMTP, but still log for evidence
            if (!emailEnabled) {
                audit.setStatus("SKIPPED");
                audit.setErrorMessage("Email disabled (app.email.enabled=false)");
                emailAuditRepository.save(audit);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();

            // From is optional; if blank, JavaMailSender may still send using default config
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }

            message.setTo(to);
            message.setSubject(subject != null ? subject : "");
            message.setText(body != null ? body : "");

            mailSender.send(message);

            audit.setStatus("SENT");
            audit.setSentAt(Instant.now());
            emailAuditRepository.save(audit);

        } catch (Exception e) {
            log.error("Email send failed to {} | subject={}", to, subject, e);

            audit.setStatus("FAILED");
            audit.setErrorMessage(e.getMessage());
            emailAuditRepository.save(audit);
        }
    }
}