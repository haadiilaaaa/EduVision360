package lk.icbt.eduvision.eduvision360.communication.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.communication.dto.CreateMessageRequest;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageResponse;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageStudentOptionResponse;
import lk.icbt.eduvision.eduvision360.communication.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/messages")
@RequiredArgsConstructor
public class TeacherMessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(messageService.sendTeacherMessage(request, authentication));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<MessageResponse>> getSentMessages(Authentication authentication) {
        return ResponseEntity.ok(messageService.getTeacherSentMessages(authentication));
    }

    @GetMapping("/students")
    public ResponseEntity<List<MessageStudentOptionResponse>> getAvailableStudents(Authentication authentication) {
        return ResponseEntity.ok(messageService.getAvailableStudentsForTeacher(authentication));
    }
}