package lk.icbt.eduvision.eduvision360.prediction.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "dropout_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "student_course_prediction_unique",
        def = "{'studentId': 1, 'courseId': 1}",
        unique = true
)
public class DropoutPrediction {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String studentEmail;

    private String courseId;
    private String courseCode;
    private String courseTitle;

    private String teacherId;
    private String teacherName;

    private Double dropoutProbability;
    private String predictedLabel;
    private String riskLevel;
    private Double threshold;

    private String predictedByUserId;
    private String predictedByRole;

    private String snapshotId;

    @Builder.Default
    private Integer windowDays = 30;

    @Builder.Default
    private Instant predictedAt = Instant.now();

    @Builder.Default
    private Map<String, Object> featureOverrides = new HashMap<>();
}