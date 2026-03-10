package lk.icbt.eduvision.eduvision360.classsession.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.classsession.dto.ClassSessionResponse;
import lk.icbt.eduvision.eduvision360.classsession.dto.CreateClassSessionRequest;
import lk.icbt.eduvision.eduvision360.classsession.dto.UpdateClassSessionStatusRequest;
import lk.icbt.eduvision.eduvision360.classsession.service.ClassSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    @PostMapping
    public ResponseEntity<ClassSessionResponse> createSession(
            @Valid @RequestBody CreateClassSessionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(classSessionService.createSession(request, authentication));
    }

    @GetMapping("/teacher/my")
    public ResponseEntity<List<ClassSessionResponse>> getTeacherSessions(Authentication authentication) {
        return ResponseEntity.ok(classSessionService.getTeacherSessions(authentication));
    }

    @GetMapping("/student/my")
    public ResponseEntity<List<ClassSessionResponse>> getStudentSessions(Authentication authentication) {
        return ResponseEntity.ok(classSessionService.getStudentSessions(authentication));
    }

    @PutMapping("/{sessionId}/status")
    public ResponseEntity<ClassSessionResponse> updateSessionStatus(
            @PathVariable String sessionId,
            @Valid @RequestBody UpdateClassSessionStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(classSessionService.updateSessionStatus(sessionId, request, authentication));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Internal server error"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}