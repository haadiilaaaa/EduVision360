package lk.icbt.eduvision.eduvision360.quiz.service;

import lk.icbt.eduvision.eduvision360.ai.dto.QuizQuestionDto;
import lk.icbt.eduvision.eduvision360.ai.model.AiInteraction;
import lk.icbt.eduvision.eduvision360.ai.model.AiInteractionType;
import lk.icbt.eduvision.eduvision360.ai.repository.AiInteractionRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.quiz.client.TeacherQuizGenerationClient;
import lk.icbt.eduvision.eduvision360.quiz.dto.*;
import lk.icbt.eduvision.eduvision360.quiz.model.TeacherQuiz;
import lk.icbt.eduvision.eduvision360.quiz.model.TeacherQuizQuestion;
import lk.icbt.eduvision.eduvision360.quiz.repository.TeacherQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherQuizServiceImpl implements TeacherQuizService {

    private static final String AI_PROVIDER = "ollama-local";

    private final TeacherQuizRepository teacherQuizRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AiInteractionRepository aiInteractionRepository;
    private final TeacherQuizGenerationClient teacherQuizGenerationClient;

    @Override
    public GeneratedTeacherQuizResponse generateDraft(TeacherGenerateQuizRequest request, Authentication authentication) {
        String teacherId = authentication.getName();

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        ensureTeacherOwnsCourse(teacherId, course);

        TeacherQuizGenerationClient.GeneratedQuizDraftResult aiResult =
                teacherQuizGenerationClient.generateDraft(
                        course.getTitle(),
                        request.topic().trim(),
                        request.difficulty().trim(),
                        request.questionCount(),
                        request.sourceText()
                );

        AiInteraction interaction = aiInteractionRepository.save(
                AiInteraction.builder()
                        .teacherId(teacher.getId())
                        .teacherName(teacher.getFullName())
                        .teacherEmail(teacher.getEmail())

                        .courseId(course.getId())
                        .courseCode(course.getCourseCode())
                        .courseTitle(course.getTitle())

                        .interactionType(AiInteractionType.TEACHER_QUIZ_GENERATION)

                        .topic(request.topic().trim())
                        .questionCount(request.questionCount())
                        .sourceTextChars(safeLen(request.sourceText()))

                        .promptText(buildPromptSummary(request))
                        .responseText("Generated " + aiResult.questions().size() + " teacher quiz questions")

                        .promptChars(safeLen(buildPromptSummary(request)))
                        .contextChars(safeLen(request.sourceText()))
                        .responseChars(safeLen("Generated " + aiResult.questions().size() + " teacher quiz questions"))
                        .contextAttached(request.sourceText() != null && !request.sourceText().isBlank())
                        .contextTruncated(false)

                        .aiLatencyMs(aiResult.aiLatencyMs())
                        .aiProvider(AI_PROVIDER)

                        .createdAt(Instant.now())
                        .build()
        );

        return new GeneratedTeacherQuizResponse(
                interaction.getId(),
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                aiResult.title(),
                request.topic().trim(),
                request.difficulty().trim(),
                request.questionCount(),
                aiResult.questions(),
                interaction.getCreatedAt(),
                aiResult.aiLatencyMs()
        );
    }

    @Override
    public TeacherQuizResponse saveQuiz(CreateTeacherQuizRequest request, Authentication authentication) {
        String teacherId = authentication.getName();

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        ensureTeacherOwnsCourse(teacherId, course);

        List<TeacherQuizQuestion> questions = request.questions().stream()
                .map(this::mapQuestion)
                .toList();

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("At least one question is required");
        }

        TeacherQuiz quiz = TeacherQuiz.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getFullName())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .title(request.title().trim())
                .description(request.description() == null ? null : request.description().trim())
                .topic(request.topic().trim())
                .difficulty(request.difficulty().trim())
                .questionCount(questions.size())
                .questionType("MCQ")
                .generatedByAi(true)
                .reviewedByTeacher(true)
                .status("DRAFT")
                .aiInteractionId(blankToNull(request.aiInteractionId()))
                .aiProvider(AI_PROVIDER)
                .questions(questions)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TeacherQuiz saved = teacherQuizRepository.save(quiz);
        return toResponse(saved);
    }

    @Override
    public List<TeacherQuizResponse> getMyQuizzes(Authentication authentication) {
        String teacherId = authentication.getName();
        return teacherQuizRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TeacherQuizResponse> getMyQuizzesByCourse(String courseId, Authentication authentication) {
        String teacherId = authentication.getName();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        ensureTeacherOwnsCourse(teacherId, course);

        return teacherQuizRepository.findByTeacherIdAndCourseIdOrderByCreatedAtDesc(teacherId, courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TeacherQuizResponse getMyQuizById(String id, Authentication authentication) {
        String teacherId = authentication.getName();

        TeacherQuiz quiz = teacherQuizRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        return toResponse(quiz);
    }

    private void ensureTeacherOwnsCourse(String teacherId, Course course) {
        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("You can only generate quizzes for your own courses");
        }
    }

    private TeacherQuizQuestion mapQuestion(CreateTeacherQuizQuestionRequest q) {
        List<String> cleanedOptions = q.options().stream()
                .map(opt -> opt == null ? "" : opt.trim())
                .filter(opt -> !opt.isBlank())
                .toList();

        if (cleanedOptions.size() < 2) {
            throw new IllegalArgumentException("Each question must have at least 2 options");
        }

        String correctAnswer = q.correctAnswer().trim();

        if (cleanedOptions.stream().noneMatch(opt -> opt.equals(correctAnswer))) {
            throw new IllegalArgumentException("Correct answer must match one of the options");
        }

        return TeacherQuizQuestion.builder()
                .questionText(q.questionText().trim())
                .options(cleanedOptions)
                .correctAnswer(correctAnswer)
                .explanation(q.explanation() == null ? null : q.explanation().trim())
                .build();
    }

    private TeacherQuizResponse toResponse(TeacherQuiz quiz) {
        List<QuizQuestionDto> questionDtos = quiz.getQuestions().stream()
                .map(q -> new QuizQuestionDto(
                        q.getQuestionText(),
                        q.getOptions(),
                        q.getCorrectAnswer(),
                        q.getExplanation()
                ))
                .toList();

        return new TeacherQuizResponse(
                quiz.getId(),
                quiz.getTeacherId(),
                quiz.getTeacherName(),
                quiz.getCourseId(),
                quiz.getCourseCode(),
                quiz.getCourseTitle(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTopic(),
                quiz.getDifficulty(),
                quiz.getQuestionCount(),
                quiz.getQuestionType(),
                quiz.getGeneratedByAi(),
                quiz.getReviewedByTeacher(),
                quiz.getStatus(),
                quiz.getAiInteractionId(),
                quiz.getAiProvider(),
                questionDtos,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }

    private int safeLen(String s) {
        return s == null ? 0 : s.length();
    }

    private String blankToNull(String s) {
        if (s == null || s.trim().isBlank()) return null;
        return s.trim();
    }

    private String buildPromptSummary(TeacherGenerateQuizRequest request) {
        return "Teacher quiz generation | topic=" + request.topic()
                + " | difficulty=" + request.difficulty()
                + " | questionCount=" + request.questionCount();
    }
}