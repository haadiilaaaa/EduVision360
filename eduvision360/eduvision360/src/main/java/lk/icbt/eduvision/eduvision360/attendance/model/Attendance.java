package lk.icbt.eduvision.eduvision360.attendance.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "student_session_unique",
        def = "{'studentId': 1, 'sessionId': 1}",
        unique = true
)
public class Attendance {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private String studentEmail;

    /**
     * Real attendance identity should be session based.
     */
    @Indexed
    private String sessionId;

    /**
     * Real course reference for analytics and filtering.
     */
    @Indexed
    private String courseId;

    /**
     * Human-readable display code.
     */
    private String courseCode;

    /**
     * Legacy alias kept only to reduce frontend breakage.
     * Store same value as courseCode.
     */
    private String classId;

    @Indexed
    private LocalDate date;

    private LocalTime time;

    @Indexed
    private AttendanceStatus status;

    private Double confidence;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}