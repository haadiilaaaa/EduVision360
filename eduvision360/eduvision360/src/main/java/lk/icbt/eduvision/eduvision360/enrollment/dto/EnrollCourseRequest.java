package lk.icbt.eduvision.eduvision360.enrollment.dto;

import jakarta.validation.constraints.NotBlank;

public record EnrollCourseRequest(
        @NotBlank String courseId
) {}