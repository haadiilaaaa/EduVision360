package lk.icbt.eduvision.eduvision360.analytics.dto;

import java.util.List;

public record BusinessAnalyticsResponse(
        long totalEnrollments,
        long enrollmentsLast30Days,
        List<DailyCount> enrollmentsTrend30Days,
        List<CourseCount> topCoursesLast30Days
) {
    public record DailyCount(String day, long count) {}
    public record CourseCount(String courseId, String courseCode, long count) {}
}