package lk.icbt.eduvision.eduvision360.feedback.dto;

import lk.icbt.eduvision.eduvision360.feedback.model.FeedbackCategory;
import lk.icbt.eduvision.eduvision360.feedback.model.FeedbackStatus;

import java.time.Instant;

public record FeedbackResponse(
        String id,
        String studentId,
        String studentName,
        String studentEmail,
        String teacherId,
        String courseId,
        String courseCode,
        String courseTitle,
        String sessionId,
        FeedbackCategory category,
        Integer rating,
        String comment,
        FeedbackStatus status,
        String responseComment,
        String responderId,
        String responderName,
        Instant createdAt,
        Instant updatedAt
) {}