package lk.icbt.eduvision.eduvision360.communication.service;

import lk.icbt.eduvision.eduvision360.communication.dto.CreateMessageRequest;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageResponse;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageStudentOptionResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MessageService {

    MessageResponse sendTeacherMessage(CreateMessageRequest request, Authentication authentication);

    List<MessageResponse> getTeacherSentMessages(Authentication authentication);

    List<MessageResponse> getStudentInbox(Authentication authentication);

    MessageResponse markAsRead(String messageId, Authentication authentication);

    List<MessageStudentOptionResponse> getAvailableStudentsForTeacher(Authentication authentication);
}