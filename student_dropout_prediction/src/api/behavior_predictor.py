from .schemas import SinglePredictionRequest, SinglePredictionResponse


BEHAVIOR_KEYS = {
    "days_since_last_login",
    "login_count_total",
    "attendance_present_count_window",
    "days_since_last_attendance",
    "material_views_count_window",
    "days_since_last_material_view",
    "ai_total_count_window",
    "ai_tutor_count_window",
    "ai_chatbot_count_window",
    "ai_summary_count_window",
    "ai_quiz_count_window",
    "days_since_last_ai_use",
}


def is_behavior_feature_request(request: SinglePredictionRequest) -> bool:
    features = request.features or {}
    return any(key in features for key in BEHAVIOR_KEYS)


def get_risk_level(probability: float) -> str:
    if probability >= 0.70:
        return "HIGH"
    elif probability >= 0.40:
        return "MEDIUM"
    return "LOW"


def to_float(value, default=0.0):
    try:
        if value is None:
            return float(default)
        return float(value)
    except Exception:
        return float(default)


def clamp(value: float, low: float = 0.0, high: float = 1.0) -> float:
    return max(low, min(high, value))


def run_behavior_prediction(request: SinglePredictionRequest) -> SinglePredictionResponse:
    features = request.features or {}

    days_since_last_login = to_float(features.get("days_since_last_login"), 9999)
    login_count_total = to_float(features.get("login_count_total"), 0)

    attendance_present_count_window = to_float(features.get("attendance_present_count_window"), 0)
    days_since_last_attendance = to_float(features.get("days_since_last_attendance"), 9999)

    material_views_count_window = to_float(features.get("material_views_count_window"), 0)
    days_since_last_material_view = to_float(features.get("days_since_last_material_view"), 9999)

    ai_total_count_window = to_float(features.get("ai_total_count_window"), 0)
    ai_tutor_count_window = to_float(features.get("ai_tutor_count_window"), 0)
    ai_chatbot_count_window = to_float(features.get("ai_chatbot_count_window"), 0)
    ai_summary_count_window = to_float(features.get("ai_summary_count_window"), 0)
    ai_quiz_count_window = to_float(features.get("ai_quiz_count_window"), 0)
    days_since_last_ai_use = to_float(features.get("days_since_last_ai_use"), 9999)

    # login risk
    login_recency_risk = clamp(days_since_last_login / 30.0)
    login_volume_risk = 1.0 - clamp(login_count_total / 20.0)
    login_risk = (0.7 * login_recency_risk) + (0.3 * login_volume_risk)

    # attendance risk
    attendance_presence_risk = 1.0 - clamp(attendance_present_count_window / 8.0)
    attendance_recency_risk = clamp(days_since_last_attendance / 30.0)
    attendance_risk = (0.65 * attendance_presence_risk) + (0.35 * attendance_recency_risk)

    # material usage risk
    material_usage_risk = 1.0 - clamp(material_views_count_window / 10.0)
    material_recency_risk = clamp(days_since_last_material_view / 30.0)
    material_risk = (0.5 * material_usage_risk) + (0.5 * material_recency_risk)

    # AI usage risk
    guided_ai_count = ai_tutor_count_window + ai_summary_count_window + ai_quiz_count_window
    ai_strength = ai_total_count_window + (0.5 * guided_ai_count) + (0.2 * ai_chatbot_count_window)

    ai_usage_risk = 1.0 - clamp(ai_strength / 10.0)
    ai_recency_risk = clamp(days_since_last_ai_use / 30.0)
    ai_risk = (0.4 * ai_usage_risk) + (0.6 * ai_recency_risk)

    # final score
    probability = (
        0.35 * attendance_risk
        + 0.25 * login_risk
        + 0.20 * material_risk
        + 0.20 * ai_risk
    )

    probability = round(clamp(probability), 4)
    threshold = 0.54

    predicted_label = "Dropout" if probability >= threshold else "Not Dropout"
    risk_level = get_risk_level(probability)

    return SinglePredictionResponse(
        studentId=request.studentId,
        studentName=request.studentName,
        courseId=request.courseId,
        dropoutProbability=probability,
        predictedLabel=predicted_label,
        riskLevel=risk_level,
        threshold=round(threshold, 2)
    )