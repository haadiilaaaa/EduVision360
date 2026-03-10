package lk.icbt.eduvision.eduvision360.prediction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropoutPredictionResponse {

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

    private String modelName;
    private String scoringMode;

    private Instant predictedAt;
}