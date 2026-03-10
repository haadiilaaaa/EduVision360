package lk.icbt.eduvision.eduvision360.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressAnalyticsResponse {

    private String studentId;
    private String studentName;
    private String studentEmail;

    private String courseId;
    private String courseCode;
    private String courseTitle;

    private Integer windowDays;

    private Double progressScore;
    private String trend;
    private String status;
    private String interventionSuggestion;

    private String dropoutRiskLevel;
    private Double dropoutProbability;

    private Long attendancePresentCountWindow;
    private Long materialViewsCountWindow;
    private Long aiTotalCountWindow;
    private Long daysSinceLastLogin;
    private Long daysSinceLastAttendance;
    private Long daysSinceLastMaterialView;
    private Long daysSinceLastAiUse;

    private Double averageEngagementScore;
    private Long attentiveCount;
    private Long neutralCount;
    private Long distractedCount;

    private List<String> reasons;

    private Instant generatedAt;
}