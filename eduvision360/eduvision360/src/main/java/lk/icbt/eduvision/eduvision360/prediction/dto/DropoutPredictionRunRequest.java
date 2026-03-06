package lk.icbt.eduvision.eduvision360.prediction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropoutPredictionRunRequest {

    private String studentId;
    private String courseId;
    private Integer windowDays;
    private Map<String, Object> featureOverrides;
}