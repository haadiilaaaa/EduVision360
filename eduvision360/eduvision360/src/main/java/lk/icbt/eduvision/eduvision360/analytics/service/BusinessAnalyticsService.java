package lk.icbt.eduvision.eduvision360.analytics.service;

import lk.icbt.eduvision.eduvision360.analytics.dto.BusinessAnalyticsResponse;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessAnalyticsService {

    private final EnrollmentRepository enrollmentRepository;

    public BusinessAnalyticsResponse getBusinessAnalytics() {

        Instant end = Instant.now();
        Instant start = end.minus(30, ChronoUnit.DAYS);

        long totalEnrollments = enrollmentRepository.count();

        List<Enrollment> recent = enrollmentRepository.findByEnrolledAtBetween(start, end);
        long enrollmentsLast30Days = recent.size();

        // Trend by day (Asia/Colombo)
        ZoneId zone = ZoneId.of("Asia/Colombo");
        Map<String, Long> trendMap = new TreeMap<>();

        for (Enrollment e : recent) {
            if (e.getEnrolledAt() == null) continue;
            String day = LocalDate.ofInstant(e.getEnrolledAt(), zone).toString();
            trendMap.put(day, trendMap.getOrDefault(day, 0L) + 1L);
        }

        List<BusinessAnalyticsResponse.DailyCount> trend = trendMap.entrySet().stream()
                .map(x -> new BusinessAnalyticsResponse.DailyCount(x.getKey(), x.getValue()))
                .toList();

        // Top courses
        Map<String, CourseAgg> courseMap = new HashMap<>();
        for (Enrollment e : recent) {
            if (e.getCourseId() == null) continue;
            CourseAgg agg = courseMap.computeIfAbsent(
                    e.getCourseId(),
                    k -> new CourseAgg(e.getCourseId(), e.getCourseCode() != null ? e.getCourseCode() : "N/A")
            );
            agg.count++;
        }

        List<BusinessAnalyticsResponse.CourseCount> topCourses = courseMap.values().stream()
                .sorted(Comparator.comparingLong((CourseAgg a) -> a.count).reversed())
                .limit(8)
                .map(a -> new BusinessAnalyticsResponse.CourseCount(a.courseId, a.courseCode, a.count))
                .collect(Collectors.toList());

        return new BusinessAnalyticsResponse(
                totalEnrollments,
                enrollmentsLast30Days,
                trend,
                topCourses
        );
    }

    private static class CourseAgg {
        String courseId;
        String courseCode;
        long count;

        CourseAgg(String courseId, String courseCode) {
            this.courseId = courseId;
            this.courseCode = courseCode;
        }
    }
}