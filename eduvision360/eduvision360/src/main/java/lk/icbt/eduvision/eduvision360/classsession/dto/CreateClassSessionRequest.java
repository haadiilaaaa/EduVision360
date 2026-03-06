package lk.icbt.eduvision.eduvision360.classsession.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateClassSessionRequest(

        @NotBlank(message = "Course ID is required")
        String courseId,

        @NotNull(message = "Session date is required")
        @FutureOrPresent(message = "Session date cannot be in the past")
        LocalDate sessionDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {}