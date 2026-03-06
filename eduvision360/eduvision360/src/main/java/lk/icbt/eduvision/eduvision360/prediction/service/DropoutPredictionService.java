package lk.icbt.eduvision.eduvision360.prediction.service;

import lk.icbt.eduvision.eduvision360.prediction.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface DropoutPredictionService {

    DropoutPredictionResponse predict(DropoutPredictionRequest request);

    DropoutPredictionResponse runAndSave(DropoutPredictionRunRequest request, Authentication authentication);

    List<PredictionStudentOptionResponse> getStudentsForCourse(String courseId, Authentication authentication);

    PredictionSummaryResponse getTeacherSummary(Authentication authentication);

    PredictionSummaryResponse getAdminSummary();

    DropoutPredictionResponse getMyLatestPrediction(Authentication authentication);

    List<DropoutPredictionResponse> getTeacherPredictionsForCourse(String courseId, Authentication authentication);

    List<DropoutPredictionResponse> getAllPredictionsForAdmin();

    // NEW
    List<DropoutPredictionResponse> getMyAllPredictions(Authentication authentication);
}