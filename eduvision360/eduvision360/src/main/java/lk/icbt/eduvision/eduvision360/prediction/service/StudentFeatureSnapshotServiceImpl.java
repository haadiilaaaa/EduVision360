package lk.icbt.eduvision.eduvision360.prediction.service;

import lk.icbt.eduvision.eduvision360.ai.model.AiInteraction;
import lk.icbt.eduvision.eduvision360.ai.model.AiInteractionType;
import lk.icbt.eduvision.eduvision360.ai.repository.AiInteractionRepository;
import lk.icbt.eduvision.eduvision360.attendance.model.Attendance;
import lk.icbt.eduvision.eduvision360.attendance.repository.AttendanceRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.material.model.MaterialViewLog;
import lk.icbt.eduvision.eduvision360.material.repository.MaterialViewLogRepository;
import lk.icbt.eduvision.eduvision360.prediction.model.StudentFeatureSnapshot;
import lk.icbt.eduvision.eduvision360.prediction.repository.StudentFeatureSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentFeatureSnapshotServiceImpl implements StudentFeatureSnapshotService {

    private final StudentFeatureSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final MaterialViewLogRepository materialViewLogRepository;
    private final AiInteractionRepository aiInteractionRepository;

    @Override
    public StudentFeatureSnapshot buildAndSaveSnapshot(
            String studentId,
            String courseId,
            Integer windowDays,
            Map<String, Object> overrides
    ) {
        int resolvedWindowDays = (windowDays == null || windowDays <= 0) ? 30 : windowDays;

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Instant now = Instant.now();
        Instant cutoffInstant = now.minus(resolvedWindowDays, ChronoUnit.DAYS);
        LocalDate cutoffDate = LocalDate.now().minusDays(resolvedWindowDays);

        Map<String, Object> features = new HashMap<>();

        // -------------------------
        // Login features
        // -------------------------
        Instant lastLoginAt = student.getLastLoginAt();

        long daysSinceLastLogin = (lastLoginAt == null)
                ? 9999
                : ChronoUnit.DAYS.between(lastLoginAt, now);

        long loginCountTotal = student.getLoginCount() == null ? 0L : student.getLoginCount();

        features.put("days_since_last_login", daysSinceLastLogin);
        features.put("login_count_total", loginCountTotal);

        // -------------------------
        // Attendance features
        // -------------------------
        List<Attendance> allAttendance = attendanceRepository.findByStudentIdOrderByDateDescTimeDesc(studentId);

        List<Attendance> courseAttendance = allAttendance.stream()
                .filter(a -> courseId.equals(a.getCourseId()))
                .toList();

        long attendancePresentCountWindow = courseAttendance.stream()
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(cutoffDate))
                .count();

        LocalDate lastAttendanceDate = courseAttendance.stream()
                .map(Attendance::getDate)
                .filter(d -> d != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        long daysSinceLastAttendance = (lastAttendanceDate == null)
                ? 9999
                : ChronoUnit.DAYS.between(lastAttendanceDate, LocalDate.now());

        features.put("attendance_present_count_window", attendancePresentCountWindow);
        features.put("days_since_last_attendance", daysSinceLastAttendance);

        // -------------------------
        // Material view features
        // -------------------------
        long materialViewsCountWindow =
                materialViewLogRepository.countByStudentIdAndCourseIdAndViewedAtAfter(
                        studentId, courseId, cutoffInstant
                );

        MaterialViewLog lastMaterialView =
                materialViewLogRepository.findTopByStudentIdAndCourseIdOrderByViewedAtDesc(studentId, courseId)
                        .orElse(null);

        long daysSinceLastMaterialView = (lastMaterialView == null || lastMaterialView.getViewedAt() == null)
                ? 9999
                : ChronoUnit.DAYS.between(lastMaterialView.getViewedAt(), now);

        features.put("material_views_count_window", materialViewsCountWindow);
        features.put("days_since_last_material_view", daysSinceLastMaterialView);

        // -------------------------
        // AI interaction features
        // -------------------------
        long aiTotalCountWindow =
                aiInteractionRepository.countByStudentIdAndCourseIdAndCreatedAtAfter(
                        studentId, courseId, cutoffInstant
                );

        long aiTutorCountWindow =
                aiInteractionRepository.countByStudentIdAndCourseIdAndInteractionTypeAndCreatedAtAfter(
                        studentId, courseId, AiInteractionType.AI_TUTOR, cutoffInstant
                );

        long aiChatbotCountWindow =
                aiInteractionRepository.countByStudentIdAndCourseIdAndInteractionTypeAndCreatedAtAfter(
                        studentId, courseId, AiInteractionType.CHATBOT, cutoffInstant
                );

        long aiSummaryCountWindow =
                aiInteractionRepository.countByStudentIdAndCourseIdAndInteractionTypeAndCreatedAtAfter(
                        studentId, courseId, AiInteractionType.SUMMARY_GENERATION, cutoffInstant
                );

        long aiQuizCountWindow =
                aiInteractionRepository.countByStudentIdAndCourseIdAndInteractionTypeAndCreatedAtAfter(
                        studentId, courseId, AiInteractionType.QUIZ_GENERATION, cutoffInstant
                );

        AiInteraction lastAiInteraction =
                aiInteractionRepository.findTopByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId)
                        .orElse(null);

        long daysSinceLastAiUse = (lastAiInteraction == null || lastAiInteraction.getCreatedAt() == null)
                ? 9999
                : ChronoUnit.DAYS.between(lastAiInteraction.getCreatedAt(), now);

        features.put("ai_total_count_window", aiTotalCountWindow);
        features.put("ai_tutor_count_window", aiTutorCountWindow);
        features.put("ai_chatbot_count_window", aiChatbotCountWindow);
        features.put("ai_summary_count_window", aiSummaryCountWindow);
        features.put("ai_quiz_count_window", aiQuizCountWindow);
        features.put("days_since_last_ai_use", daysSinceLastAiUse);

        // -------------------------
        // Merge overrides
        // -------------------------
        Map<String, Object> safeOverrides = overrides == null ? Map.of() : overrides;
        Map<String, Object> finalFeatures = new HashMap<>(features);
        finalFeatures.putAll(safeOverrides);

        StudentFeatureSnapshot snapshot = StudentFeatureSnapshot.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .windowDays(resolvedWindowDays)
                .features(features)
                .overrides(new HashMap<>(safeOverrides))
                .finalFeatures(finalFeatures)
                .createdAt(Instant.now())
                .build();

        return snapshotRepository.save(snapshot);
    }
}