package lk.icbt.eduvision.eduvision360.prediction.client;

import lk.icbt.eduvision.eduvision360.prediction.dto.DropoutPredictionRequest;
import lk.icbt.eduvision.eduvision360.prediction.dto.DropoutPredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class DropoutPredictionClient {

    private final RestTemplate restTemplate;

    @Value("${dropout.api.base-url}")
    private String dropoutApiBaseUrl;

    public DropoutPredictionResponse predict(DropoutPredictionRequest request) {
        String url = dropoutApiBaseUrl + "/predict";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DropoutPredictionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<DropoutPredictionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DropoutPredictionResponse.class
        );

        return response.getBody();
    }
}