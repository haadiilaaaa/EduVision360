package lk.icbt.eduvision.eduvision360.engagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngagementAnalysisResponse {

    private String studentId;
    private String studentName;
    private String studentEmail;

    private String sessionId;
    private String courseId;

    private String label;
    private Double confidence;
    private Double engagementScore;

    private Boolean faceDetected;
    private Integer faceCount;
    private Boolean centeredFace;
    private Boolean eyesDetected;
    private Boolean lookingAway;

    // ✅ NEW (emotion)
    private String dominantEmotion;
    private Double emotionConfidence;
    private String modelType;

    private String message;
    private Instant capturedAt;
}