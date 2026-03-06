package lk.icbt.eduvision.eduvision360.course.dto;

import lk.icbt.eduvision.eduvision360.course.model.CourseStatus;

import java.time.Instant;

public record CourseResponse(
        String id,
        String courseCode,
        String title,
        String description,
        String departmentId,
        String departmentName,
        String teacherId,
        String teacherName,
        Integer creditValue,
        Integer semester,
        String academicYear,
        CourseStatus status,
        Instant createdAt
) {}