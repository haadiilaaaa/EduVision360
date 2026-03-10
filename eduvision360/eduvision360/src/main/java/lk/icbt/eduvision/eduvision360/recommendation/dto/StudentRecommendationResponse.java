package lk.icbt.eduvision.eduvision360.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRecommendationResponse {

    private String courseId;
    private String courseCode;
    private String courseTitle;

    private Double progressScore;
    private String progressStatus;
    private String riskLevel;
    private String trend;

    private List<RecommendationItemDto> items;
}