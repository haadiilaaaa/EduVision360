package lk.icbt.eduvision.eduvision360.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FaceAiService {

    private final RestTemplate restTemplate;

    private static final String AI_URL = "http://localhost:8000/register-face";

    public Object registerFace(String studentId, MultipartFile[] images) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("student_id", studentId);

            for (MultipartFile file : images) {
                ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("files", resource);
            }

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Object> response =
                    restTemplate.postForEntity(AI_URL, request, Object.class);

            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read image files", e);
        }
    }
}
