package lk.icbt.eduvision.eduvision360.classsession.dto;

import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ClassSessionResponse(
        String id,
        String courseId,
        String courseCode,
        String courseTitle,
        String teacherId,
        String teacherName,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        ClassSessionStatus status,
        Instant createdAt
) {}