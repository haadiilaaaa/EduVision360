package lk.icbt.eduvision.eduvision360.announcement.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "course_announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAnnouncement {

    @Id
    private String id;

    private String courseId;
    private String teacherId;

    private String title;
    private String message;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}