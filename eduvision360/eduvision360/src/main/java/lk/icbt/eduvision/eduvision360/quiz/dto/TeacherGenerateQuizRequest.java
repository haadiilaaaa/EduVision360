package lk.icbt.eduvision.eduvision360.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeacherGenerateQuizRequest(
        @NotBlank(message = "Course ID is required")
        String courseId,

        @NotBlank(message = "Topic is required")
        String topic,

        @NotBlank(message = "Difficulty is required")
        String difficulty,

        @NotNull(message = "Question count is required")
        @Min(value = 1, message = "Question count must be at least 1")
        @Max(value = 10, message = "Question count must be at most 10")
        Integer questionCount,

        // optional
        String sourceText
) {
}