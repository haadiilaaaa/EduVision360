package lk.icbt.eduvision.eduvision360.material.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.icbt.eduvision.eduvision360.material.model.MaterialType;
import lombok.Data;

@Data
public class CreateMaterialRequest {

    @NotBlank(message = "Course is required")
    private String courseId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Material type is required")
    private MaterialType type;

    @NotBlank(message = "Content is required")
    private String content;
}