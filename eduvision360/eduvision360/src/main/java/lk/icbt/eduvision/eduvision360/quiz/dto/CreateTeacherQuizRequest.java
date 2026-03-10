package lk.icbt.eduvision.eduvision360.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateTeacherQuizRequest(
        @NotBlank(message = "Course ID is required")
        String courseId,

        String aiInteractionId,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotBlank(message = "Topic is required")
        String topic,

        @NotBlank(message = "Difficulty is required")
        String difficulty,

        @Valid
        @NotEmpty(message = "At least one question is required")
        List<CreateTeacherQuizQuestionRequest> questions
) {
}