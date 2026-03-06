package lk.icbt.eduvision.eduvision360.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnnouncementRequest {

    @NotBlank(message = "Course is required")
    private String courseId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;
}