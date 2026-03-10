package lk.icbt.eduvision.eduvision360.recommendation.controller;

import lk.icbt.eduvision.eduvision360.recommendation.dto.StudentRecommendationResponse;
import lk.icbt.eduvision.eduvision360.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/student/my")
    public ResponseEntity<List<StudentRecommendationResponse>> getMyRecommendations(
            Authentication authentication
    ) {
        return ResponseEntity.ok(recommendationService.getMyRecommendations(authentication));
    }
}