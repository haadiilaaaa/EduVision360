package lk.icbt.eduvision.eduvision360.feedback.service;

import lk.icbt.eduvision.eduvision360.feedback.dto.CreateFeedbackRequest;
import lk.icbt.eduvision.eduvision360.feedback.dto.FeedbackResponse;
import lk.icbt.eduvision.eduvision360.feedback.dto.UpdateFeedbackStatusRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface FeedbackService {

    FeedbackResponse submitFeedback(CreateFeedbackRequest request, Authentication authentication);

    List<FeedbackResponse> getMyFeedback(Authentication authentication);

    List<FeedbackResponse> getTeacherFeedback(Authentication authentication);

    List<FeedbackResponse> getAllFeedback();

    FeedbackResponse updateTeacherFeedbackStatus(
            String feedbackId,
            UpdateFeedbackStatusRequest request,
            Authentication authentication
    );

    FeedbackResponse updateAdminFeedbackStatus(
            String feedbackId,
            UpdateFeedbackStatusRequest request,
            Authentication authentication
    );
}