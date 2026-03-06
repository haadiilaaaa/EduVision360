package lk.icbt.eduvision.eduvision360.engagement.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.engagement.client.EngagementClient;
import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementAnalysisResponse;
import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementSummaryResponse;
import lk.icbt.eduvision.eduvision360.engagement.model.EngagementLog;
import lk.icbt.eduvision.eduvision360.engagement.repository.EngagementLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EngagementServiceImpl implements EngagementService {

    private final EngagementClient engagementClient;
    private final EngagementLogRepository engagementLogRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public EngagementAnalysisResponse analyzeAndSave(
            MultipartFile image,
            String sessionId,
            String courseId,
            Authentication authentication
    ) {
        String studentId = authentication.getName();

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (courseId != null && !courseId.isBlank()) {
            courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        }

        EngagementAnalysisResponse aiResponse = engagementClient.analyze(
                image,
                studentId,
                sessionId,
                courseId
        );

        EngagementLog log = EngagementLog.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .courseId(courseId)
                .sessionId(sessionId)
                .label(aiResponse.getLabel())
                .confidence(aiResponse.getConfidence())
                .engagementScore(aiResponse.getEngagementScore())
                .faceDetected(aiResponse.getFaceDetected())
                .faceCount(aiResponse.getFaceCount())
                .centeredFace(aiResponse.getCenteredFace())
                .eyesDetected(aiResponse.getEyesDetected())
                .lookingAway(aiResponse.getLookingAway())

                // ✅ NEW (emotion)
                .dominantEmotion(aiResponse.getDominantEmotion())
                .emotionConfidence(aiResponse.getEmotionConfidence())
                .modelType(aiResponse.getModelType())

                .message(aiResponse.getMessage())
                .capturedAt(Instant.now())
                .build();

        EngagementLog saved = engagementLogRepository.save(log);
        return toResponse(saved);
    }

    @Override
    public EngagementSummaryResponse getAdminSummary() {
        List<EngagementLog> all = engagementLogRepository.findAll();
        List<EngagementAnalysisResponse> recent = engagementLogRepository.findTop10ByOrderByCapturedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();

        return new EngagementSummaryResponse(
                all.size(),
                countLabel(all, "ATTENTIVE"),
                countLabel(all, "NEUTRAL"),
                countLabel(all, "DISTRACTED"),
                averageScore(all),
                recent
        );
    }

    @Override
    public EngagementSummaryResponse getTeacherSummary(String courseId, Authentication authentication) {
        String teacherId = authentication.getName();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("You can only access engagement analytics for your own courses");
        }

        List<EngagementLog> logs = engagementLogRepository.findByCourseIdOrderByCapturedAtDesc(courseId);
        List<EngagementAnalysisResponse> recent = engagementLogRepository
                .findTop10ByCourseIdOrderByCapturedAtDesc(courseId)
                .stream()
                .map(this::toResponse)
                .toList();

        return new EngagementSummaryResponse(
                logs.size(),
                countLabel(logs, "ATTENTIVE"),
                countLabel(logs, "NEUTRAL"),
                countLabel(logs, "DISTRACTED"),
                averageScore(logs),
                recent
        );
    }

    @Override
    public EngagementSummaryResponse getStudentSummary(Authentication authentication) {
        String studentId = authentication.getName();

        List<EngagementLog> logs = engagementLogRepository.findByStudentIdOrderByCapturedAtDesc(studentId);
        List<EngagementAnalysisResponse> recent = engagementLogRepository
                .findTop10ByStudentIdOrderByCapturedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return new EngagementSummaryResponse(
                logs.size(),
                countLabel(logs, "ATTENTIVE"),
                countLabel(logs, "NEUTRAL"),
                countLabel(logs, "DISTRACTED"),
                averageScore(logs),
                recent
        );
    }

    private long countLabel(List<EngagementLog> logs, String label) {
        return logs.stream()
                .filter(log -> label.equalsIgnoreCase(log.getLabel()))
                .count();
    }

    private double averageScore(List<EngagementLog> logs) {
        return logs.stream()
                .map(EngagementLog::getEngagementScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private EngagementAnalysisResponse toResponse(EngagementLog log) {
        return new EngagementAnalysisResponse(
                log.getStudentId(),
                log.getStudentName(),
                log.getStudentEmail(),
                log.getSessionId(),
                log.getCourseId(),
                log.getLabel(),
                log.getConfidence(),
                log.getEngagementScore(),
                log.getFaceDetected(),
                log.getFaceCount(),
                log.getCenteredFace(),
                log.getEyesDetected(),
                log.getLookingAway(),

                // ✅ NEW (emotion)
                log.getDominantEmotion(),
                log.getEmotionConfidence(),
                log.getModelType(),

                log.getMessage(),
                log.getCapturedAt()
        );
    }
}