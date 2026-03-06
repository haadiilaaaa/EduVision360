package lk.icbt.eduvision.eduvision360.prediction.controller;

import lk.icbt.eduvision.eduvision360.prediction.dto.*;
import lk.icbt.eduvision.eduvision360.prediction.service.DropoutPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class DropoutPredictionController {

    private final DropoutPredictionService dropoutPredictionService;

    @PostMapping("/dropout")
    public ResponseEntity<DropoutPredictionResponse> predictDropout(
            @RequestBody DropoutPredictionRequest request
    ) {
        return ResponseEntity.ok(dropoutPredictionService.predict(request));
    }

    @PostMapping("/dropout/run")
    public ResponseEntity<DropoutPredictionResponse> runAndSavePrediction(
            @RequestBody DropoutPredictionRunRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(dropoutPredictionService.runAndSave(request, authentication));
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<List<PredictionStudentOptionResponse>> getStudentsForCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(dropoutPredictionService.getStudentsForCourse(courseId, authentication));
    }

    @GetMapping("/teacher/summary")
    public ResponseEntity<PredictionSummaryResponse> getTeacherSummary(Authentication authentication) {
        return ResponseEntity.ok(dropoutPredictionService.getTeacherSummary(authentication));
    }

    @GetMapping("/admin/summary")
    public ResponseEntity<PredictionSummaryResponse> getAdminSummary() {
        return ResponseEntity.ok(dropoutPredictionService.getAdminSummary());
    }

    @GetMapping("/student/my-latest")
    public ResponseEntity<DropoutPredictionResponse> getMyLatestPrediction(Authentication authentication) {
        return ResponseEntity.ok(dropoutPredictionService.getMyLatestPrediction(authentication));
    }

    // NEW
    @GetMapping("/student/my-all")
    public ResponseEntity<List<DropoutPredictionResponse>> getMyAllPredictions(Authentication authentication) {
        return ResponseEntity.ok(dropoutPredictionService.getMyAllPredictions(authentication));
    }

    @GetMapping("/teacher/course/{courseId}")
    public ResponseEntity<List<DropoutPredictionResponse>> getTeacherPredictionsForCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(dropoutPredictionService.getTeacherPredictionsForCourse(courseId, authentication));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<DropoutPredictionResponse>> getAllPredictionsForAdmin() {
        return ResponseEntity.ok(dropoutPredictionService.getAllPredictionsForAdmin());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
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