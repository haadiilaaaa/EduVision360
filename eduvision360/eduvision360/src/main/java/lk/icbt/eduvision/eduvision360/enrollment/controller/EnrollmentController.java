package lk.icbt.eduvision.eduvision360.enrollment.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.enrollment.dto.EnrollCourseRequest;
import lk.icbt.eduvision.eduvision360.enrollment.dto.EnrollmentResponse;
import lk.icbt.eduvision.eduvision360.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollCourseRequest req, Authentication authentication) {
        return enrollmentService.enroll(req, authentication);
    }

    @GetMapping("/my")
    public List<EnrollmentResponse> my(Authentication authentication) {
        return enrollmentService.myEnrollments(authentication);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public Map<String, String> handleConflict(IllegalStateException ex) {
        return Map.of("message", ex.getMessage());
    }
}