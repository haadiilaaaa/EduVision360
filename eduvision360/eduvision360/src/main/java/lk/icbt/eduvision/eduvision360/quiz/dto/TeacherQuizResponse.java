package lk.icbt.eduvision.eduvision360.quiz.dto;

import lk.icbt.eduvision.eduvision360.ai.dto.QuizQuestionDto;

import java.time.Instant;
import java.util.List;

public record TeacherQuizResponse(
        String id,
        String teacherId,
        String teacherName,
        String courseId,
        String courseCode,
        String courseTitle,
        String title,
        String description,
        String topic,
        String difficulty,
        Integer questionCount,
        String questionType,
        Boolean generatedByAi,
        Boolean reviewedByTeacher,
        String status,
        String aiInteractionId,
        String aiProvider,
        List<QuizQuestionDto> questions,
        Instant createdAt,
        Instant updatedAt
) {
}