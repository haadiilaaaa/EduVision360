package lk.icbt.eduvision.eduvision360.quiz.client;

import lk.icbt.eduvision.eduvision360.ai.dto.QuizQuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TeacherQuizGenerationClient {

    private final RestTemplate restTemplate;

    @Value("${ai.api.base-url:http://localhost:8001/ai}")
    private String aiApiBaseUrl;

    public GeneratedQuizDraftResult generateDraft(
            String courseTitle,
            String topic,
            String difficulty,
            Integer questionCount,
            String sourceText
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("course_title", courseTitle);
        payload.put("topic", topic);
        payload.put("difficulty", difficulty);
        payload.put("question_count", questionCount);
        payload.put("source_text", sourceText == null ? "" : sourceText.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        long start = System.currentTimeMillis();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                aiApiBaseUrl + "/teacher-quiz",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        long latency = System.currentTimeMillis() - start;

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("AI quiz generation returned empty response");
        }

        String title = body.get("title") != null
                ? String.valueOf(body.get("title"))
                : topic + " Quiz";

        List<Map<String, Object>> rawQuestions =
                (List<Map<String, Object>>) body.get("questions");

        if (rawQuestions == null || rawQuestions.isEmpty()) {
            throw new IllegalStateException("AI quiz generation returned no questions");
        }

        List<QuizQuestionDto> questions = rawQuestions.stream().map(q ->
                new QuizQuestionDto(
                        String.valueOf(q.get("question")),
                        (List<String>) q.get("options"),
                        String.valueOf(q.get("correctAnswer")),
                        String.valueOf(q.get("explanation"))
                )
        ).toList();

        return new GeneratedQuizDraftResult(title, questions, latency);
    }

    public record GeneratedQuizDraftResult(
            String title,
            List<QuizQuestionDto> questions,
            Long aiLatencyMs
    ) {
    }
}