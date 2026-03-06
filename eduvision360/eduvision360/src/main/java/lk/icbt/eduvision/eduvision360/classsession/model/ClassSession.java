package lk.icbt.eduvision.eduvision360.classsession.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "class_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "course_session_unique",
        def = "{'courseId': 1, 'sessionDate': 1, 'startTime': 1}",
        unique = true
)
public class ClassSession {

    @Id
    private String id;

    private String courseId;
    private String courseCode;
    private String courseTitle;

    private String teacherId;
    private String teacherName;

    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Builder.Default
    private ClassSessionStatus status = ClassSessionStatus.SCHEDULED;

    @Builder.Default
    private Instant createdAt = Instant.now();
}