package lk.icbt.eduvision.eduvision360.recommendation.service;

import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.material.model.LearningMaterial;
import lk.icbt.eduvision.eduvision360.material.repository.LearningMaterialRepository;
import lk.icbt.eduvision.eduvision360.progress.model.StudentProgressAnalytics;
import lk.icbt.eduvision.eduvision360.progress.repository.StudentProgressAnalyticsRepository;
import lk.icbt.eduvision.eduvision360.recommendation.dto.RecommendationItemDto;
import lk.icbt.eduvision.eduvision360.recommendation.dto.StudentRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentProgressAnalyticsRepository progressRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<StudentRecommendationResponse> getMyRecommendations(Authentication authentication) {
        String studentId = authentication.getName();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);
        if (enrollments.isEmpty()) {
            return List.of();
        }

        Map<String, StudentProgressAnalytics> analyticsByCourseId =
                progressRepository.findByStudentIdOrderByGeneratedAtDesc(studentId)
                        .stream()
                        .collect(Collectors.toMap(
                                StudentProgressAnalytics::getCourseId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        return enrollments.stream()
                .map(enrollment -> buildRecommendationForEnrollment(enrollment, analyticsByCourseId.get(enrollment.getCourseId())))
                .toList();
    }

    private StudentRecommendationResponse buildRecommendationForEnrollment(
            Enrollment enrollment,
            StudentProgressAnalytics analytics
    ) {
        Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);

        String courseId = enrollment.getCourseId();
        String courseCode = course != null ? course.getCourseCode() : enrollment.getCourseCode();
        String courseTitle = course != null ? course.getTitle() : "Course";

        List<LearningMaterial> courseMaterials =
                learningMaterialRepository.findByCourseIdOrderByCreatedAtDesc(courseId);

        LearningMaterial latestMaterial = courseMaterials.isEmpty() ? null : courseMaterials.get(0);

        if (analytics == null) {
            List<RecommendationItemDto> fallbackItems = new ArrayList<>();

            if (latestMaterial != null) {
                fallbackItems.add(new RecommendationItemDto(
                        "MATERIAL",
                        "MEDIUM",
                        "Review available course materials",
                        "Progress analytics has not been generated yet for this course, but you can begin with the available learning materials.",
                        "Open Materials",
                        "OPEN_COURSE_MATERIALS",
                        latestMaterial.getId(),
                        latestMaterial.getTitle()
                ));
            } else {
                fallbackItems.add(new RecommendationItemDto(
                        "PROGRESS",
                        "LOW",
                        "Wait for analytics or continue learning activity",
                        "No progress analytics has been generated yet for this course.",
                        "View Progress",
                        "VIEW_PROGRESS",
                        null,
                        null
                ));
            }

            fallbackItems.add(new RecommendationItemDto(
                    "AI_TUTOR",
                    "LOW",
                    "Use AI tutor for course support",
                    "You can use the AI tutor for questions and guided revision in this course.",
                    "Open AI Tutor",
                    "OPEN_AI_TUTOR",
                    null,
                    null
            ));

            return new StudentRecommendationResponse(
                    courseId,
                    courseCode,
                    courseTitle,
                    null,
                    "NOT_GENERATED",
                    null,
                    null,
                    fallbackItems
            );
        }

        return buildRecommendationFromAnalytics(analytics, latestMaterial);
    }

    private StudentRecommendationResponse buildRecommendationFromAnalytics(
            StudentProgressAnalytics analytics,
            LearningMaterial latestMaterial
    ) {
        List<RecommendationItemDto> items = new ArrayList<>();

        String riskLevel = safeUpper(analytics.getDropoutRiskLevel());
        String status = safeUpper(analytics.getStatus());
        String trend = safeUpper(analytics.getTrend());

        if ("HIGH".equals(riskLevel) || "AT_RISK".equals(status)) {
            addIfMissing(items, new RecommendationItemDto(
                    "TEACHER_SUPPORT",
                    "HIGH",
                    "Seek teacher follow-up support",
                    "Your current analytics indicate that additional academic monitoring may be beneficial.",
                    "View Progress",
                    "VIEW_PROGRESS",
                    null,
                    null
            ));

            if (latestMaterial != null) {
                addIfMissing(items, new RecommendationItemDto(
                        "MATERIAL",
                        "HIGH",
                        "Review recent course material",
                        "Your course analytics suggest that revisiting learning materials may help improve performance.",
                        "Open Materials",
                        "OPEN_COURSE_MATERIALS",
                        latestMaterial.getId(),
                        latestMaterial.getTitle()
                ));
            }

            addIfMissing(items, new RecommendationItemDto(
                    "AI_TUTOR",
                    "MEDIUM",
                    "Use AI tutor for guided revision",
                    "AI-supported tutoring can help reinforce difficult concepts step by step.",
                    "Open AI Tutor",
                    "OPEN_AI_TUTOR",
                    null,
                    null
            ));
        }

        if (analytics.getMaterialViewsCountWindow() != null && analytics.getMaterialViewsCountWindow() <= 1) {
            if (latestMaterial != null) {
                addIfMissing(items, new RecommendationItemDto(
                        "MATERIAL",
                        "HIGH",
                        "Review course materials regularly",
                        "Material interaction is currently limited for this course.",
                        "Open Materials",
                        "OPEN_COURSE_MATERIALS",
                        latestMaterial.getId(),
                        latestMaterial.getTitle()
                ));
            } else {
                addIfMissing(items, new RecommendationItemDto(
                        "MATERIAL",
                        "MEDIUM",
                        "Check available learning materials",
                        "Material interaction is currently limited for this course.",
                        "Open Materials",
                        "OPEN_COURSE_MATERIALS",
                        null,
                        null
                ));
            }
        }

        if ((analytics.getAttendancePresentCountWindow() != null && analytics.getAttendancePresentCountWindow() <= 1)
                || (analytics.getDaysSinceLastAttendance() != null && analytics.getDaysSinceLastAttendance() > 14)) {
            addIfMissing(items, new RecommendationItemDto(
                    "ATTENDANCE",
                    "HIGH",
                    "Improve attendance consistency",
                    "Recent attendance activity is low and stronger participation is recommended.",
                    "View Progress",
                    "VIEW_PROGRESS",
                    null,
                    null
            ));
        }

        if (analytics.getAiTotalCountWindow() != null && analytics.getAiTotalCountWindow() == 0) {
            addIfMissing(items, new RecommendationItemDto(
                    "AI_TUTOR",
                    "MEDIUM",
                    "Use the AI tutor for revision",
                    "AI-supported learning activity is limited for this course.",
                    "Open AI Tutor",
                    "OPEN_AI_TUTOR",
                    null,
                    null
            ));
        }

        if (analytics.getAverageEngagementScore() != null && analytics.getAverageEngagementScore() < 0.45) {
            addIfMissing(items, new RecommendationItemDto(
                    "PROGRESS",
                    "MEDIUM",
                    "Use short focused study sessions",
                    "Recent engagement evidence is weak, so shorter and more guided revision may help.",
                    "View Progress",
                    "VIEW_PROGRESS",
                    null,
                    null
            ));
        }

        if (items.isEmpty() && "ON_TRACK".equals(status) && "LOW".equals(riskLevel)) {
            if (latestMaterial != null) {
                items.add(new RecommendationItemDto(
                        "MATERIAL",
                        "LOW",
                        "Continue your current learning momentum",
                        "Your learning indicators are stable. Continue reviewing course materials to stay on track.",
                        "Open Materials",
                        "OPEN_COURSE_MATERIALS",
                        latestMaterial.getId(),
                        latestMaterial.getTitle()
                ));
            } else {
                items.add(new RecommendationItemDto(
                        "PROGRESS",
                        "LOW",
                        "Maintain your current learning consistency",
                        "Your current analytics suggest that your course progress is stable.",
                        "View Progress",
                        "VIEW_PROGRESS",
                        null,
                        null
                ));
            }
        }

        if (items.isEmpty()) {
            items.add(new RecommendationItemDto(
                    "PROGRESS",
                    "LOW",
                    "Maintain steady learning progress",
                    "No major intervention signals were detected for this course at the moment.",
                    "View Progress",
                    "VIEW_PROGRESS",
                    null,
                    null
            ));
        }

        List<RecommendationItemDto> limitedItems = items.stream()
                .sorted(Comparator.comparingInt(this::priorityRank))
                .limit(4)
                .toList();

        return new StudentRecommendationResponse(
                analytics.getCourseId(),
                analytics.getCourseCode(),
                analytics.getCourseTitle(),
                analytics.getProgressScore(),
                analytics.getStatus(),
                analytics.getDropoutRiskLevel(),
                analytics.getTrend(),
                limitedItems
        );
    }

    private void addIfMissing(List<RecommendationItemDto> items, RecommendationItemDto candidate) {
        boolean exists = items.stream()
                .anyMatch(item -> item.getType() != null
                        && item.getType().equalsIgnoreCase(candidate.getType()));
        if (!exists) {
            items.add(candidate);
        }
    }

    private int priorityRank(RecommendationItemDto item) {
        String priority = safeUpper(item.getPriority());
        return switch (priority) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            default -> 3;
        };
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}