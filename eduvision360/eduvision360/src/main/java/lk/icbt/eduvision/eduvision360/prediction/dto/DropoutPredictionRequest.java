package lk.icbt.eduvision.eduvision360.prediction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropoutPredictionRequest {

    private String studentId;
    private String studentName;
    private String courseId;
    private Map<String, Object> features;
}