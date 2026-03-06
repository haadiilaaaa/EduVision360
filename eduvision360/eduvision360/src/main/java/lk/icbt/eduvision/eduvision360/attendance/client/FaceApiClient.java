package lk.icbt.eduvision.eduvision360.attendance.client;

import lk.icbt.eduvision.eduvision360.attendance.dto.FaceVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class FaceApiClient {

    private final RestClient restClient = RestClient.create();

    @Value("${app.face.api.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${app.face.api.verify-path:/verify-face}")
    private String verifyPath;

    public FaceVerificationResponse verify(byte[] imageBytes) {

        String url = baseUrl + verifyPath;

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();

        // IMPORTANT: field name must be exactly "file"
        form.add("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "capture.jpg";
            }
        });

        return restClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(FaceVerificationResponse.class);
    }
}