package lk.icbt.eduvision.eduvision360.progress.controller;

import lk.icbt.eduvision.eduvision360.progress.dto.ProgressAnalyticsResponse;
import lk.icbt.eduvision.eduvision360.progress.dto.ProgressAnalyticsRunRequest;
import lk.icbt.eduvision.eduvision360.progress.service.StudentProgressAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress-analytics")
@RequiredArgsConstructor
public class StudentProgressAnalyticsController {

    private final StudentProgressAnalyticsService studentProgressAnalyticsService;

    @PostMapping("/run")
    public ResponseEntity<ProgressAnalyticsResponse> runAndSave(
            @RequestBody ProgressAnalyticsRunRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studentProgressAnalyticsService.runAndSave(request, authentication));
    }

    @GetMapping("/student/my")
    public ResponseEntity<List<ProgressAnalyticsResponse>> getMyProgress(Authentication authentication) {
        return ResponseEntity.ok(studentProgressAnalyticsService.getMyProgress(authentication));
    }

    @GetMapping("/teacher/course/{courseId}")
    public ResponseEntity<List<ProgressAnalyticsResponse>> getTeacherCourseProgress(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studentProgressAnalyticsService.getTeacherCourseProgress(courseId, authentication));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ProgressAnalyticsResponse>> getAllProgressForAdmin() {
        return ResponseEntity.ok(studentProgressAnalyticsService.getAllProgressForAdmin());
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