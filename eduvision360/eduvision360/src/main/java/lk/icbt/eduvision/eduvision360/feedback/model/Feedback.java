package lk.icbt.eduvision.eduvision360.feedback.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private String studentName;
    private String studentEmail;

    @Indexed
    private String teacherId;

    @Indexed
    private String courseId;

    private String courseCode;
    private String courseTitle;

    // optional for future session-linked feedback
    private String sessionId;

    private FeedbackCategory category;

    private Integer rating;

    private String comment;

    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.NEW;

    private String responseComment;
    private String responderId;
    private String responderName;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}