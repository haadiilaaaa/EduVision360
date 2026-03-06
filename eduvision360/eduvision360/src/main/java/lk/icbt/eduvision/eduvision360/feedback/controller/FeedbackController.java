package lk.icbt.eduvision.eduvision360.feedback.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.feedback.dto.CreateFeedbackRequest;
import lk.icbt.eduvision.eduvision360.feedback.dto.FeedbackResponse;
import lk.icbt.eduvision.eduvision360.feedback.dto.UpdateFeedbackStatusRequest;
import lk.icbt.eduvision.eduvision360.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/api/student/feedback")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody CreateFeedbackRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(feedbackService.submitFeedback(request, authentication));
    }

    @GetMapping("/api/student/feedback/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(Authentication authentication) {
        return ResponseEntity.ok(feedbackService.getMyFeedback(authentication));
    }

    @GetMapping("/api/teacher/feedback/my-courses")
    public ResponseEntity<List<FeedbackResponse>> getTeacherFeedback(Authentication authentication) {
        return ResponseEntity.ok(feedbackService.getTeacherFeedback(authentication));
    }

    @PutMapping("/api/teacher/feedback/{feedbackId}/status")
    public ResponseEntity<FeedbackResponse> updateTeacherFeedbackStatus(
            @PathVariable String feedbackId,
            @Valid @RequestBody UpdateFeedbackStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                feedbackService.updateTeacherFeedbackStatus(feedbackId, request, authentication)
        );
    }

    @GetMapping("/api/admin/feedback")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @PutMapping("/api/admin/feedback/{feedbackId}/status")
    public ResponseEntity<FeedbackResponse> updateAdminFeedbackStatus(
            @PathVariable String feedbackId,
            @Valid @RequestBody UpdateFeedbackStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                feedbackService.updateAdminFeedbackStatus(feedbackId, request, authentication)
        );
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