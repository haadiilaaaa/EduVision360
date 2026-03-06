package lk.icbt.eduvision.eduvision360.material.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "material_view_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialViewLog {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private String studentName;
    private String studentEmail;

    @Indexed
    private String materialId;

    @Indexed
    private String courseId;

    private String courseCode;
    private String courseTitle;

    private String teacherId;

    private String materialTitle;
    private MaterialType materialType;

    @Indexed
    @Builder.Default
    private Instant viewedAt = Instant.now();
}