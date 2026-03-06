package lk.icbt.eduvision.eduvision360.engagement.client;

import lk.icbt.eduvision.eduvision360.engagement.dto.EngagementAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class EngagementClient {

    @Value("${ai.service.base-url:http://localhost:8001}")
    private String aiServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public EngagementAnalysisResponse analyze(
            MultipartFile image,
            String studentId,
            String sessionId,
            String courseId
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null
                            ? image.getOriginalFilename()
                            : "engagement.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            if (studentId != null) body.add("studentId", studentId);
            if (sessionId != null) body.add("sessionId", sessionId);
            if (courseId != null) body.add("courseId", courseId);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<EngagementAnalysisResponse> response = restTemplate.postForEntity(
                    aiServiceBaseUrl + "/engagement/analyze",
                    requestEntity,
                    EngagementAnalysisResponse.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from AI engagement service");
            }

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call engagement analysis API: " + e.getMessage(), e);
        }
    }
}