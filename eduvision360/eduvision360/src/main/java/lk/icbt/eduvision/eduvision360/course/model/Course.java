package lk.icbt.eduvision.eduvision360.course.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private String id;

    @Indexed(unique = true)
    private String courseCode;

    private String title;

    private String description;

    private String departmentId;

    private String teacherId;

    private Integer creditValue;

    private Integer semester;

    private String academicYear;

    @Builder.Default
    private CourseStatus status = CourseStatus.ACTIVE;

    @Builder.Default
    private Instant createdAt = Instant.now();
}