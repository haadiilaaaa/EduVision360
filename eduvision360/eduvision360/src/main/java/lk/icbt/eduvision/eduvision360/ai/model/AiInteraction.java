package lk.icbt.eduvision.eduvision360.ai.model;

import lk.icbt.eduvision.eduvision360.material.model.MaterialType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "ai_interactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInteraction {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private String studentName;
    private String studentEmail;

    @Indexed
    private String courseId;

    private String courseCode;
    private String courseTitle;

    @Indexed
    private AiInteractionType interactionType;

    // -----------------------
    // Material context metadata
    // -----------------------
    @Indexed
    private String materialId;

    private String materialTitle;

    private MaterialType materialType;

    /**
     * True if the request sent any contextText to AI.
     */
    private Boolean contextAttached;

    /**
     * True if contextText was truncated by backend sanitize step.
     */
    private Boolean contextTruncated;

    // -----------------------
    // Request/response metrics
    // -----------------------
    private Integer promptChars;
    private Integer contextChars;
    private Integer responseChars;

    // For summary/quiz analytics
    private String topic;
    private Integer questionCount;
    private Integer sourceTextChars;

    // Optional system info
    private Long aiLatencyMs;
    private String aiProvider; // e.g., "ollama-local"

    // -----------------------
    // Main content
    // -----------------------
    private String promptText;
    private String responseText;

    @Indexed
    @Builder.Default
    private Instant createdAt = Instant.now();
    private String teacherId;

    private String teacherName;
    private String teacherEmail;
}