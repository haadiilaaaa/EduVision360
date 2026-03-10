package lk.icbt.eduvision.eduvision360.communication.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.communication.dto.CreateMessageRequest;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageResponse;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageStudentOptionResponse;
import lk.icbt.eduvision.eduvision360.communication.model.DirectMessage;
import lk.icbt.eduvision.eduvision360.communication.repository.DirectMessageRepository;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.EmailService;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public MessageResponse sendTeacherMessage(CreateMessageRequest request, Authentication authentication) {
        String teacherId = authentication.getName();

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Only teachers can send messages from this endpoint");
        }

        User student = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Teachers can only send messages to students");
        }

        DirectMessage message = DirectMessage.builder()
                .senderId(teacher.getId())
                .senderName(teacher.getFullName())
                .senderRole(teacher.getRole())
                .receiverId(student.getId())
                .receiverName(student.getFullName())
                .receiverRole(student.getRole())
                .subject(request.getSubject().trim())
                .body(request.getBody().trim())
                .read(false)
                .readAt(null)
                .createdAt(Instant.now())
                .build();

        DirectMessage saved = directMessageRepository.save(message);

        log.info("[MESSAGE_SENT] teacherId={} studentId={} messageId={}",
                teacher.getId(), student.getId(), saved.getId());

        notificationService.createNotification(
                student.getId(),
                "New message from teacher",
                "You have received a new message: " + saved.getSubject(),
                NotificationType.SYSTEM_ALERT,
                NotificationPriority.HIGH,
                saved.getId(),
                "DIRECT_MESSAGE",
                "/student/messages"
        );

        if (student.getEmail() != null && !student.getEmail().isBlank()) {
            emailService.sendEmail(
                    student.getEmail(),
                    "EduVision360: New Message from Teacher",
                    "Hello " + (student.getFullName() != null ? student.getFullName() : "") + ",\n\n"
                            + "You have received a new message from " + teacher.getFullName() + ".\n\n"
                            + "Subject: " + saved.getSubject() + "\n\n"
                            + saved.getBody() + "\n\n"
                            + "Please log in to EduVision360 to view your messages.\n\n"
                            + "Regards,\nEduVision360",
                    "DIRECT_MESSAGE",
                    saved.getId(),
                    "DIRECT_MESSAGE"
            );
        } else {
            log.warn("[MESSAGE_EMAIL_NOT_SENT] studentId={} reason=no_email messageId={}",
                    student.getId(), saved.getId());
        }

        return toResponse(saved);
    }

    @Override
    public List<MessageResponse> getTeacherSentMessages(Authentication authentication) {
        String teacherId = authentication.getName();

        return directMessageRepository.findBySenderIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MessageResponse> getStudentInbox(Authentication authentication) {
        String studentId = authentication.getName();

        return directMessageRepository.findByReceiverIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MessageResponse markAsRead(String messageId, Authentication authentication) {
        String studentId = authentication.getName();

        DirectMessage message = directMessageRepository.findByIdAndReceiverId(messageId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.isRead()) {
            message.setRead(true);
            message.setReadAt(Instant.now());
        }

        DirectMessage saved = directMessageRepository.save(message);

        log.info("[MESSAGE_READ] studentId={} messageId={}", studentId, messageId);

        return toResponse(saved);
    }

    @Override
    public List<MessageStudentOptionResponse> getAvailableStudentsForTeacher(Authentication authentication) {
        String teacherId = authentication.getName();

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Only teachers can access this endpoint");
        }

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT)
                .map(u -> new MessageStudentOptionResponse(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getStudentCode()
                ))
                .toList();
    }

    private MessageResponse toResponse(DirectMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getSenderName(),
                message.getSenderRole(),
                message.getReceiverId(),
                message.getReceiverName(),
                message.getReceiverRole(),
                message.getSubject(),
                message.getBody(),
                message.isRead(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}