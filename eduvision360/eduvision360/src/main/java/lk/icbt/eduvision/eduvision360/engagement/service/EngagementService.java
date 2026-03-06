package lk.icbt.eduvision.eduvision360.engagement.service;

import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementAnalysisResponse;
import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementSummaryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface EngagementService {

    EngagementAnalysisResponse analyzeAndSave(
            MultipartFile image,
            String sessionId,
            String courseId,
            Authentication authentication
    );

    EngagementSummaryResponse getAdminSummary();

    EngagementSummaryResponse getTeacherSummary(String courseId, Authentication authentication);

    EngagementSummaryResponse getStudentSummary(Authentication authentication);
}