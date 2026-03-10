package lk.icbt.eduvision.eduvision360.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOverviewDto {
    private long totalStudents;
    private long totalTeachers;
    private long totalAdmins;
    private long pendingTeachers;

    private long totalDepartments;
    private long totalCourses;
    private long totalSessions;

    private long totalQuizzes;
    private long totalAnnouncements;
    private long totalMaterials;
    private long totalMessages;

    private long highRiskCount;
    private long mediumRiskCount;
    private long lowRiskCount;
}