package lk.icbt.eduvision.eduvision360.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateTeacherQuizQuestionRequest(
        @NotBlank(message = "Question text is required")
        String questionText,

        @NotEmpty(message = "Options are required")
        List<String> options,

        @NotBlank(message = "Correct answer is required")
        String correctAnswer,

        String explanation
) {
}