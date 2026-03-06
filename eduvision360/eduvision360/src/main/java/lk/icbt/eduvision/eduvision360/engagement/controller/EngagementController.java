package lk.icbt.eduvision.eduvision360.engagement.controller;

import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementAnalysisResponse;
import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementSummaryResponse;
import lk.icbt.eduvision.eduvision360.engagement.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/engagement")
@RequiredArgsConstructor
public class EngagementController {

    private final EngagementService engagementService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EngagementAnalysisResponse analyze(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "courseId", required = false) String courseId,
            Authentication authentication
    ) {
        return engagementService.analyzeAndSave(image, sessionId, courseId, authentication);
    }

    @GetMapping("/admin/summary")
    public EngagementSummaryResponse getAdminSummary() {
        return engagementService.getAdminSummary();
    }

    @GetMapping("/teacher/summary")
    public EngagementSummaryResponse getTeacherSummary(
            @RequestParam("courseId") String courseId,
            Authentication authentication
    ) {
        return engagementService.getTeacherSummary(courseId, authentication);
    }

    @GetMapping("/student/summary")
    public EngagementSummaryResponse getStudentSummary(Authentication authentication) {
        return engagementService.getStudentSummary(authentication);
    }
}