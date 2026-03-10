package lk.icbt.eduvision.eduvision360.progress.service;

import lk.icbt.eduvision.eduvision360.progress.dto.ProgressAnalyticsResponse;
import lk.icbt.eduvision.eduvision360.progress.dto.ProgressAnalyticsRunRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface StudentProgressAnalyticsService {

    ProgressAnalyticsResponse runAndSave(ProgressAnalyticsRunRequest request, Authentication authentication);

    List<ProgressAnalyticsResponse> getMyProgress(Authentication authentication);

    List<ProgressAnalyticsResponse> getTeacherCourseProgress(String courseId, Authentication authentication);

    List<ProgressAnalyticsResponse> getAllProgressForAdmin();
}