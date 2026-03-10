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

    @Value("${app.mail.from:}")
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
        if (to == null || to.isBlank()) {
            EmailAudit audit = EmailAudit.builder()
                    .toEmail(to)
                    .subject(subject)
                    .body(body)
                    .type(type)
                    .relatedEntityId(relatedEntityId)
                    .relatedEntityType(relatedEntityType)
                    .status("FAILED")
                    .errorMessage("Recipient email is blank")
                    .createdAt(Instant.now())
                    .build();

            emailAuditRepository.save(audit);
            log.warn("[EMAIL_FAILED] type={} reason=blank_recipient subject={}", type, subject);
            return;
        }

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
            if (!emailEnabled) {
                audit.setStatus("SKIPPED");
                audit.setErrorMessage("Email disabled (app.email.enabled=false)");
                emailAuditRepository.save(audit);

                log.warn("[EMAIL_SKIPPED] type={} to={} subject={} reason=app.email.enabled=false",
                        type, to, subject);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();

            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }

            message.setTo(to);
            message.setSubject(subject != null ? subject : "");
            message.setText(body != null ? body : "");

            log.info("[EMAIL_ATTEMPT] type={} to={} subject={} relatedEntityType={} relatedEntityId={}",
                    type, to, subject, relatedEntityType, relatedEntityId);

            mailSender.send(message);

            audit.setStatus("SENT");
            audit.setSentAt(Instant.now());
            emailAuditRepository.save(audit);

            log.info("[EMAIL_SENT] type={} to={} subject={} relatedEntityType={} relatedEntityId={}",
                    type, to, subject, relatedEntityType, relatedEntityId);

        } catch (Exception e) {
            log.error("[EMAIL_FAILED] type={} to={} subject={} relatedEntityType={} relatedEntityId={} error={}",
                    type, to, subject, relatedEntityType, relatedEntityId, e.getMessage(), e);

            audit.setStatus("FAILED");
            audit.setErrorMessage(e.getMessage());
            emailAuditRepository.save(audit);
        }
    }
}