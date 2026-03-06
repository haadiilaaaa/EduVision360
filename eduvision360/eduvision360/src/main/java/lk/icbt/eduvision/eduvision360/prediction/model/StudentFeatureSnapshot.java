package lk.icbt.eduvision.eduvision360.prediction.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "student_feature_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "student_course_window_created_idx",
        def = "{'studentId': 1, 'courseId': 1, 'windowDays': 1, 'createdAt': -1}"
)
public class StudentFeatureSnapshot {

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

    @Builder.Default
    private Integer windowDays = 30;

    @Builder.Default
    private Map<String, Object> features = new HashMap<>();

    @Builder.Default
    private Map<String, Object> overrides = new HashMap<>();

    @Builder.Default
    private Map<String, Object> finalFeatures = new HashMap<>();

    @Builder.Default
    private Instant createdAt = Instant.now();
}