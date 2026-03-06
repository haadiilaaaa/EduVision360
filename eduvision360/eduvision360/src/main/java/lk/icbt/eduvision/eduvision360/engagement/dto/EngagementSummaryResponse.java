package lk.icbt.eduvision.eduvision360.engagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementSummaryResponse {

    private long totalLogs;
    private long attentiveCount;
    private long neutralCount;
    private long distractedCount;
    private double averageScore;
    private List<EngagementAnalysisResponse> recentLogs;
}