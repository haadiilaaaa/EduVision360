package lk.icbt.eduvision.eduvision360.communication.controller;

import lk.icbt.eduvision.eduvision360.communication.dto.MessageResponse;
import lk.icbt.eduvision.eduvision360.communication.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/messages")
@RequiredArgsConstructor
public class StudentMessageController {

    private final MessageService messageService;

    @GetMapping("/inbox")
    public ResponseEntity<List<MessageResponse>> getInbox(Authentication authentication) {
        return ResponseEntity.ok(messageService.getStudentInbox(authentication));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            @PathVariable String messageId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(messageService.markAsRead(messageId, authentication));
    }
}