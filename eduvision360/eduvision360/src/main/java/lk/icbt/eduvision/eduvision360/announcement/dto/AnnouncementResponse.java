package lk.icbt.eduvision.eduvision360.announcement.dto;

import java.time.Instant;

public record AnnouncementResponse(
        String id,
        String courseId,
        String courseCode,
        String courseTitle,
        String teacherId,
        String teacherName,
        String title,
        String message,
        Instant createdAt,
        Instant updatedAt
) {
}