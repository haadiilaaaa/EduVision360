from typing import List

from .preprocess import prepare_dataframe
from .schemas import SinglePredictionRequest, SinglePredictionResponse


def get_risk_level(probability: float) -> str:
    if probability >= 0.70:
        return "HIGH"
    elif probability >= 0.40:
        return "MEDIUM"
    return "LOW"


def run_single_prediction(
    request: SinglePredictionRequest,
    model,
    metadata,
    training_medians
) -> SinglePredictionResponse:
    feature_columns = metadata["feature_columns"]
    threshold = float(metadata.get("threshold", 0.50))
    model_name = metadata.get("model_name", "XGBoost")

    X_input = prepare_dataframe(
        records=[request.features],
        feature_columns=feature_columns,
        training_medians=training_medians
    )

    probability = float(model.predict_proba(X_input)[:, 1][0])
    predicted_label = "Dropout" if probability >= threshold else "Not Dropout"
    risk_level = get_risk_level(probability)

    return SinglePredictionResponse(
        studentId=request.studentId,
        studentName=request.studentName,
        courseId=request.courseId,
        dropoutProbability=round(probability, 4),
        predictedLabel=predicted_label,
        riskLevel=risk_level,
        threshold=round(threshold, 2),
        modelName=model_name,
        scoringMode="ML_MODEL"
    )


def run_batch_prediction(
    requests: List[SinglePredictionRequest],
    model,
    metadata,
    training_medians
) -> List[SinglePredictionResponse]:
    feature_columns = metadata["feature_columns"]
    threshold = float(metadata.get("threshold", 0.50))
    model_name = metadata.get("model_name", "XGBoost")

    X_input = prepare_dataframe(
        records=[req.features for req in requests],
        feature_columns=feature_columns,
        training_medians=training_medians
    )

    probabilities = model.predict_proba(X_input)[:, 1]

    responses = []

    for request, probability in zip(requests, probabilities):
        probability = float(probability)
        predicted_label = "Dropout" if probability >= threshold else "Not Dropout"
        risk_level = get_risk_level(probability)

        responses.append(
            SinglePredictionResponse(
                studentId=request.studentId,
                studentName=request.studentName,
                courseId=request.courseId,
                dropoutProbability=round(probability, 4),
                predictedLabel=predicted_label,
                riskLevel=risk_level,
                threshold=round(threshold, 2),
                modelName=model_name,
                scoringMode="ML_MODEL"
            )
        )

    return responses