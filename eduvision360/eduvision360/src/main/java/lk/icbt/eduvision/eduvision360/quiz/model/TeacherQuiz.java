package lk.icbt.eduvision.eduvision360.quiz.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "teacher_quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherQuiz {

    @Id
    private String id;

    private String teacherId;
    private String teacherName;

    private String courseId;
    private String courseCode;
    private String courseTitle;

    private String title;
    private String description;
    private String topic;
    private String difficulty;

    private Integer questionCount;

    @Builder.Default
    private String questionType = "MCQ";

    @Builder.Default
    private Boolean generatedByAi = true;

    @Builder.Default
    private Boolean reviewedByTeacher = true;

    @Builder.Default
    private String status = "DRAFT";

    private String aiInteractionId;
    private String aiProvider;

    private List<TeacherQuizQuestion> questions;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}