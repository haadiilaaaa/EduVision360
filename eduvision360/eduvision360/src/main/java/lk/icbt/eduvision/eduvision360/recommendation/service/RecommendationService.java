package lk.icbt.eduvision.eduvision360.recommendation.service;

import lk.icbt.eduvision.eduvision360.recommendation.dto.StudentRecommendationResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface RecommendationService {

    List<StudentRecommendationResponse> getMyRecommendations(Authentication authentication);
}