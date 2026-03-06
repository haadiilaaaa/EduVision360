package lk.icbt.eduvision.eduvision360.engagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "engagement_logs")
public class EngagementLog {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String studentEmail;

    private String courseId;
    private String sessionId;

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