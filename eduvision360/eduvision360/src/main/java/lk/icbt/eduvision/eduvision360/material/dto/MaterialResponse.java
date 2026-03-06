package lk.icbt.eduvision.eduvision360.material.dto;

import lk.icbt.eduvision.eduvision360.material.model.MaterialType;

import java.time.Instant;

public record MaterialResponse(
        String id,
        String courseId,
        String courseCode,
        String courseTitle,
        String teacherId,
        String teacherName,
        String title,
        String description,
        MaterialType type,
        String content,
        Instant createdAt
) {
}