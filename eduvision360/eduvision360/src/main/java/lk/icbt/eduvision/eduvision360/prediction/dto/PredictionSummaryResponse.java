package lk.icbt.eduvision.eduvision360.prediction.dto;

import java.util.List;

public record PredictionSummaryResponse(
        long totalPredictions,
        long highRiskCount,
        long mediumRiskCount,
        long lowRiskCount,
        List<DropoutPredictionResponse> recentPredictions
) {}