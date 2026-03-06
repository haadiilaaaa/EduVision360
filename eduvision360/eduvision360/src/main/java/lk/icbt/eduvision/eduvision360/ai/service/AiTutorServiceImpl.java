package lk.icbt.eduvision.eduvision360.ai.service;

import lk.icbt.eduvision.eduvision360.ai.dto.*;
import lk.icbt.eduvision.eduvision360.ai.model.AiInteraction;
import lk.icbt.eduvision.eduvision360.ai.model.AiInteractionType;
import lk.icbt.eduvision.eduvision360.ai.repository.AiInteractionRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.material.model.LearningMaterial;
import lk.icbt.eduvision.eduvision360.material.repository.LearningMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiTutorServiceImpl implements AiTutorService {

    private final AiInteractionRepository aiInteractionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    // NEW
    private final LearningMaterialRepository learningMaterialRepository;

    private final RestTemplate restTemplate;

    private static final String AI_BASE_URL = "http://localhost:8001/ai";
    private static final String AI_PROVIDER = "ollama-local";

    @Override
    public AiTextResponse askChatbot(AskAiRequest request, Authentication authentication) {
        return askAndSave(request, authentication, AiInteractionType.CHATBOT, "CHATBOT");
    }

    @Override
    public AiTextResponse askTutor(AskAiRequest request, Authentication authentication) {
        return askAndSave(request, authentication, AiInteractionType.AI_TUTOR, "AI_TUTOR");
    }

    @Override
    public AiTextResponse generateSummary(GenerateSummaryRequest request, Authentication authentication) {
        User student = getStudent(authentication);
        Course course = getCourse(request.courseId());
        ensureEnrollment(student.getId(), course.getId());

        LearningMaterial material = resolveMaterialIfProvided(student.getId(), course.getId(), request.materialId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "topic", request.topic(),
                "source_text", request.sourceText()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        long start = System.currentTimeMillis();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                AI_BASE_URL + "/summary",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        long latency = System.currentTimeMillis() - start;

        String responseText = String.valueOf(response.getBody().get("responseText"));

        AiInteraction saved = aiInteractionRepository.save(
                AiInteraction.builder()
                        .studentId(student.getId())
                        .studentName(student.getFullName())
                        .studentEmail(student.getEmail())
                        .courseId(course.getId())
                        .courseCode(course.getCourseCode())
                        .courseTitle(course.getTitle())
                        .interactionType(AiInteractionType.SUMMARY_GENERATION)

                        .materialId(material != null ? material.getId() : null)
                        .materialTitle(material != null ? material.getTitle() : null)
                        .materialType(material != null ? material.getType() : null)

                        .topic(request.topic())
                        .sourceTextChars(safeLen(request.sourceText()))
                        .promptText(request.topic())
                        .responseText(responseText)

                        .promptChars(safeLen(request.topic()))
                        .contextChars(null)
                        .responseChars(safeLen(responseText))
                        .contextAttached(false)
                        .contextTruncated(false)

                        .aiLatencyMs(latency)
                        .aiProvider(AI_PROVIDER)

                        .createdAt(Instant.now())
                        .build()
        );

        return new AiTextResponse(
                saved.getId(),
                saved.getCourseId(),
                saved.getCourseCode(),
                saved.getCourseTitle(),
                saved.getInteractionType(),
                saved.getPromptText(),
                saved.getResponseText(),
                saved.getCreatedAt(),

                saved.getMaterialId(),
                saved.getMaterialTitle(),
                saved.getMaterialType(),
                saved.getContextAttached(),
                saved.getContextTruncated(),
                saved.getPromptChars(),
                saved.getContextChars(),
                saved.getResponseChars(),
                saved.getAiLatencyMs()
        );
    }

    @Override
    public AiQuizResponse generateQuiz(GenerateQuizRequest request, Authentication authentication) {
        User student = getStudent(authentication);
        Course course = getCourse(request.courseId());
        ensureEnrollment(student.getId(), course.getId());

        LearningMaterial material = resolveMaterialIfProvided(student.getId(), course.getId(), request.materialId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "topic", request.topic(),
                "source_text", request.sourceText(),
                "question_count", request.questionCount()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        long start = System.currentTimeMillis();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                AI_BASE_URL + "/quiz",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        long latency = System.currentTimeMillis() - start;

        List<Map<String, Object>> rawQuestions = (List<Map<String, Object>>) response.getBody().get("questions");

        List<QuizQuestionDto> questions = rawQuestions.stream().map(q ->
                new QuizQuestionDto(
                        String.valueOf(q.get("question")),
                        (List<String>) q.get("options"),
                        String.valueOf(q.get("correctAnswer")),
                        String.valueOf(q.get("explanation"))
                )
        ).toList();

        AiInteraction saved = aiInteractionRepository.save(
                AiInteraction.builder()
                        .studentId(student.getId())
                        .studentName(student.getFullName())
                        .studentEmail(student.getEmail())
                        .courseId(course.getId())
                        .courseCode(course.getCourseCode())
                        .courseTitle(course.getTitle())
                        .interactionType(AiInteractionType.QUIZ_GENERATION)

                        .materialId(material != null ? material.getId() : null)
                        .materialTitle(material != null ? material.getTitle() : null)
                        .materialType(material != null ? material.getType() : null)

                        .topic(request.topic())
                        .questionCount(request.questionCount())
                        .sourceTextChars(safeLen(request.sourceText()))
                        .promptText(request.topic())
                        .responseText("Generated " + questions.size() + " quiz questions")

                        .promptChars(safeLen(request.topic()))
                        .contextChars(null)
                        .responseChars(safeLen("Generated " + questions.size() + " quiz questions"))
                        .contextAttached(false)
                        .contextTruncated(false)

                        .aiLatencyMs(latency)
                        .aiProvider(AI_PROVIDER)

                        .createdAt(Instant.now())
                        .build()
        );

        return new AiQuizResponse(
                saved.getId(),
                saved.getCourseId(),
                saved.getCourseCode(),
                saved.getCourseTitle(),
                saved.getInteractionType(),
                saved.getPromptText(),
                questions,
                saved.getCreatedAt(),

                saved.getMaterialId(),
                saved.getMaterialTitle(),
                saved.getMaterialType(),
                saved.getQuestionCount(),
                saved.getSourceTextChars(),
                saved.getAiLatencyMs()
        );
    }

    @Override
    public List<AiTextResponse> getMyAiHistory(Authentication authentication) {
        String studentId = authentication.getName();

        return aiInteractionRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(item -> new AiTextResponse(
                        item.getId(),
                        item.getCourseId(),
                        item.getCourseCode(),
                        item.getCourseTitle(),
                        item.getInteractionType(),
                        item.getPromptText(),
                        item.getResponseText(),
                        item.getCreatedAt(),

                        item.getMaterialId(),
                        item.getMaterialTitle(),
                        item.getMaterialType(),
                        item.getContextAttached(),
                        item.getContextTruncated(),
                        item.getPromptChars(),
                        item.getContextChars(),
                        item.getResponseChars(),
                        item.getAiLatencyMs()
                ))
                .toList();
    }

    private AiTextResponse askAndSave(
            AskAiRequest request,
            Authentication authentication,
            AiInteractionType interactionType,
            String mode
    ) {
        User student = getStudent(authentication);
        Course course = getCourse(request.courseId());
        ensureEnrollment(student.getId(), course.getId());

        LearningMaterial material = resolveMaterialIfProvided(student.getId(), course.getId(), request.materialId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        SanitizeResult sanitizeResult = sanitizeContext(request.contextText());
        String cleanedContext = sanitizeResult.cleaned;
        boolean truncated = sanitizeResult.truncated;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mode", mode);
        payload.put("course_title", course.getTitle());
        payload.put("question", request.question());
        payload.put("context_text", cleanedContext);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        long start = System.currentTimeMillis();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                AI_BASE_URL + "/ask",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        long latency = System.currentTimeMillis() - start;

        String responseText = String.valueOf(response.getBody().get("responseText"));

        String prompt = request.question() != null ? request.question().trim() : "";

        AiInteraction saved = aiInteractionRepository.save(
                AiInteraction.builder()
                        .studentId(student.getId())
                        .studentName(student.getFullName())
                        .studentEmail(student.getEmail())
                        .courseId(course.getId())
                        .courseCode(course.getCourseCode())
                        .courseTitle(course.getTitle())
                        .interactionType(interactionType)

                        .materialId(material != null ? material.getId() : null)
                        .materialTitle(material != null ? material.getTitle() : null)
                        .materialType(material != null ? material.getType() : null)

                        .promptText(prompt)
                        .responseText(responseText)

                        .promptChars(safeLen(prompt))
                        .contextChars(safeLen(cleanedContext))
                        .responseChars(safeLen(responseText))

                        .contextAttached(cleanedContext != null && !cleanedContext.isBlank())
                        .contextTruncated(truncated)

                        .aiLatencyMs(latency)
                        .aiProvider(AI_PROVIDER)

                        .createdAt(Instant.now())
                        .build()
        );

        return new AiTextResponse(
                saved.getId(),
                saved.getCourseId(),
                saved.getCourseCode(),
                saved.getCourseTitle(),
                saved.getInteractionType(),
                saved.getPromptText(),
                saved.getResponseText(),
                saved.getCreatedAt(),

                saved.getMaterialId(),
                saved.getMaterialTitle(),
                saved.getMaterialType(),
                saved.getContextAttached(),
                saved.getContextTruncated(),
                saved.getPromptChars(),
                saved.getContextChars(),
                saved.getResponseChars(),
                saved.getAiLatencyMs()
        );
    }

    private User getStudent(Authentication authentication) {
        return userRepository.findById(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    private Course getCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    private void ensureEnrollment(String studentId, String courseId) {
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!enrolled) {
            throw new AccessDeniedException("You can only use AI features for your enrolled courses");
        }
    }

    private LearningMaterial resolveMaterialIfProvided(String studentId, String courseId, String materialId) {
        if (materialId == null || materialId.trim().isBlank()) {
            return null;
        }

        LearningMaterial material = learningMaterialRepository.findById(materialId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));

        if (!courseId.equals(material.getCourseId())) {
            throw new IllegalArgumentException("Selected material does not belong to this course");
        }

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!enrolled) {
            throw new AccessDeniedException("You are not allowed to use this material");
        }

        return material;
    }

    private static int safeLen(String s) {
        return (s == null) ? 0 : s.length();
    }

    private record SanitizeResult(String cleaned, boolean truncated) {}

    private SanitizeResult sanitizeContext(String rawContext) {
        if (rawContext == null) return new SanitizeResult(null, false);

        String cleaned = rawContext.trim();
        if (cleaned.isBlank()) return new SanitizeResult(null, false);

        int maxLength = 2500;
        if (cleaned.length() > maxLength) {
            return new SanitizeResult(cleaned.substring(0, maxLength), true);
        }

        return new SanitizeResult(cleaned, false);
    }
}