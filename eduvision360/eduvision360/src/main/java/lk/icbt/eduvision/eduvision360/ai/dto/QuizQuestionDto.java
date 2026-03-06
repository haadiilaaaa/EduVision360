package lk.icbt.eduvision.eduvision360.ai.dto;

import java.util.List;

public record QuizQuestionDto(
        String question,
        List<String> options,
        String correctAnswer,
        String explanation
) {}