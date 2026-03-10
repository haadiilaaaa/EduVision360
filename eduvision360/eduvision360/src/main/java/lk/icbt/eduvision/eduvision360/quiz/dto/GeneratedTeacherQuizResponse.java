package lk.icbt.eduvision.eduvision360.quiz.dto;

import lk.icbt.eduvision.eduvision360.ai.dto.QuizQuestionDto;

import java.time.Instant;
import java.util.List;

public record GeneratedTeacherQuizResponse(
        String aiInteractionId,
        String courseId,
        String courseCode,
        String courseTitle,
        String title,
        String topic,
        String difficulty,
        Integer questionCount,
        List<QuizQuestionDto> questions,
        Instant createdAt,
        Long aiLatencyMs
) {
}