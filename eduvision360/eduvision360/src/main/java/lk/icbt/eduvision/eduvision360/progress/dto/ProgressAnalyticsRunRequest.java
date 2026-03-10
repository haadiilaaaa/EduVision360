package lk.icbt.eduvision.eduvision360.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressAnalyticsRunRequest {

    private String studentId;
    private String courseId;
    private Integer windowDays;
}