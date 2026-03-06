package lk.icbt.eduvision.eduvision360.enrollment.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "student_course_unique",
        def = "{'studentId': 1, 'courseId': 1}",
        unique = true
)
public class Enrollment {

    @Id
    private String id;

    private String studentId;     // JWT subject (your userId)
    private String studentEmail;

    private String courseId;      // Mongo course _id
    private String courseCode;    // SE01 (store for display)

    @Builder.Default
    private Instant enrolledAt = Instant.now();
}