package lk.icbt.eduvision.eduvision360.ai.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.ai.dto.*;
import lk.icbt.eduvision.eduvision360.ai.service.AiTutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/ai")
@RequiredArgsConstructor
public class AiTutorController {

    private final AiTutorService aiTutorService;

    @PostMapping("/chatbot")
    public ResponseEntity<AiTextResponse> askChatbot(
            @Valid @RequestBody AskAiRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(aiTutorService.askChatbot(request, authentication));
    }

    @PostMapping("/tutor")
    public ResponseEntity<AiTextResponse> askTutor(
            @Valid @RequestBody AskAiRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(aiTutorService.askTutor(request, authentication));
    }

    @PostMapping("/summary")
    public ResponseEntity<AiTextResponse> generateSummary(
            @Valid @RequestBody GenerateSummaryRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(aiTutorService.generateSummary(request, authentication));
    }

    @PostMapping("/quiz")
    public ResponseEntity<AiQuizResponse> generateQuiz(
            @Valid @RequestBody GenerateQuizRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(aiTutorService.generateQuiz(request, authentication));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AiTextResponse>> getMyAiHistory(Authentication authentication) {
        return ResponseEntity.ok(aiTutorService.getMyAiHistory(authentication));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Internal server error"));
    }
}