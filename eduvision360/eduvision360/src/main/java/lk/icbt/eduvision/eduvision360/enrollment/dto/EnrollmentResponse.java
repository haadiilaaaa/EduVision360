package lk.icbt.eduvision.eduvision360.enrollment.dto;

import java.time.Instant;

public record EnrollmentResponse(
        String id,
        String courseId,
        String courseCode,
        String title,
        String departmentName,
        String teacherName,
        Instant enrolledAt
) {}