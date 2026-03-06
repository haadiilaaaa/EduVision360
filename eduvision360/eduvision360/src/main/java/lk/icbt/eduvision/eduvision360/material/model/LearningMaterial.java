package lk.icbt.eduvision.eduvision360.material.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "learning_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningMaterial {

    @Id
    private String id;

    private String courseId;
    private String teacherId;

    private String title;
    private String description;

    private MaterialType type;

    // NOTE  -> actual note text
    // LINK / PDF_LINK -> URL
    private String content;

    @Builder.Default
    private Instant createdAt = Instant.now();
}