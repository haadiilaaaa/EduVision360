package lk.icbt.eduvision.eduvision360.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDto {

    private String type;          // MATERIAL / AI_TUTOR / ATTENDANCE / TEACHER_SUPPORT / PROGRESS
    private String priority;      // HIGH / MEDIUM / LOW
    private String title;
    private String reason;

    private String actionLabel;   // Open Materials / Use AI Tutor / Improve Attendance
    private String actionType;    // OPEN_COURSE_MATERIALS / OPEN_AI_TUTOR / VIEW_PROGRESS

    private String materialId;    // optional
    private String materialTitle; // optional
}