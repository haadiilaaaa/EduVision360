package lk.icbt.eduvision.eduvision360.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lk.icbt.eduvision.eduvision360.course.model.CourseStatus;

@Data
public class UpdateCourseRequest {

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must be at most 20 characters")
    private String courseCode;

    @NotBlank(message = "Course title is required")
    @Size(max = 150, message = "Course title must be at most 150 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotBlank(message = "Department ID is required")
    private String departmentId;

    private String teacherId;

    @NotNull(message = "Credit value is required")
    @Min(value = 1, message = "Credit value must be at least 1")
    private Integer creditValue;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester must be at most 8")
    private Integer semester;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Course status is required")
    private CourseStatus status;
}