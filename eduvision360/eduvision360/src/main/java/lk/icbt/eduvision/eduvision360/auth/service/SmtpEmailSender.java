package lk.icbt.eduvision.eduvision360.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendRegistrationOtp(String email, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(email);
        msg.setSubject("EduVision 360 – Verify Your Account");
        msg.setText("""
        Welcome to EduVision 360!

        Your verification OTP is:
        %s

        This OTP expires in 5 minutes.
        """.formatted(otp));

        mailSender.send(msg);
    }

    @Override
    public void sendPasswordResetLink(String email, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(email);
        msg.setSubject("EduVision 360 – Password Reset");
        msg.setText("""
        Dear User,

        Click the secure link below to reset your password:
        %s

        This link expires in 5 minutes.
        If you didn’t request this, please ignore this email.
        """.formatted(resetLink));

        mailSender.send(msg);
    }
    // ✅ Teacher Approved Email
    @Override
    public void sendTeacherApprovedEmail(String toEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("EduVision 360 – Teacher Account Approved");

        msg.setText("""
            Dear Teacher,

            Your EduVision 360 teacher account has been approved by the administrator.
            You can now log in and access your dashboard.

            Regards,
            EduVision 360 Team
        """);

        mailSender.send(msg);
    }

    // ✅ Teacher Rejected Email
    @Override
    public void sendTeacherRejectedEmail(String toEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("EduVision 360 – Teacher Account Rejected");

        msg.setText("""
            Dear Applicant,

            We regret to inform you that your teacher account request
            has been rejected by the administrator.

            Regards,
            EduVision 360 Team
        """);

        mailSender.send(msg);
    }
}

