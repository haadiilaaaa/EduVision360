package lk.icbt.eduvision.eduvision360.progress.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "student_progress_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "student_course_progress_unique",
        def = "{'studentId': 1, 'courseId': 1}",
        unique = true
)
public class StudentProgressAnalytics {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String studentEmail;

    private String courseId;
    private String courseCode;
    private String courseTitle;

    @Builder.Default
    private Integer windowDays = 30;

    private Double progressScore;              // 0 - 100
    private String trend;                      // IMPROVING / STABLE / DECLINING
    private String status;                     // ON_TRACK / NEEDS_ATTENTION / AT_RISK
    private String interventionSuggestion;

    private String dropoutRiskLevel;           // LOW / MEDIUM / HIGH
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

    private String snapshotId;

    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    @Builder.Default
    private Instant generatedAt = Instant.now();
}