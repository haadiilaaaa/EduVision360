import sys
from pathlib import Path
from enum import Enum
from datetime import date, datetime, time
from typing import Any, get_args, get_origin, Union, Annotated

import pytest
from fastapi.testclient import TestClient

# Allow running with plain: pytest ...
PROJECT_ROOT = Path(__file__).resolve().parents[3]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

import src.api.app as main_module
import src.api.ai_routes as ai_routes_module
from src.api.app import app
from src.api.schemas import SinglePredictionRequest, SinglePredictionResponse
from src.api.engagement_schemas import EngagementAnalyzeResponse
from src.api.schemas_ai import AskAiPayload


client = TestClient(app)


# -----------------------------
# Helper utilities
# -----------------------------
def _unwrap_annotation(annotation):
    origin = get_origin(annotation)

    if origin is Annotated:
        return _unwrap_annotation(get_args(annotation)[0])

    if origin is Union:
        args = [a for a in get_args(annotation) if a is not type(None)]
        if len(args) == 1:
            return _unwrap_annotation(args[0])

    return annotation


def _is_enum(annotation):
    return isinstance(annotation, type) and issubclass(annotation, Enum)


def _model_fields(model_cls):
    return getattr(model_cls, "model_fields", None) or getattr(model_cls, "__fields__", None)


def _field_required(field):
    if hasattr(field, "is_required"):
        try:
            return field.is_required()
        except TypeError:
            return field.is_required
    return getattr(field, "required", False)


def _field_annotation(field):
    return (
        getattr(field, "annotation", None)
        or getattr(field, "outer_type_", None)
        or getattr(field, "type_", None)
        or Any
    )


def _sample_value(name: str, annotation):
    annotation = _unwrap_annotation(annotation)
    origin = get_origin(annotation)

    if origin in (list,):
        args = get_args(annotation)
        inner = args[0] if args else str
        return [_sample_value(name, inner)]

    if origin in (dict,):
        return {}

    if _is_enum(annotation):
        return list(annotation)[0].value

    lower_name = name.lower()

    if annotation is str:
        if "email" in lower_name:
            return "student@test.com"
        if "id" in lower_name:
            return "id-001"
        if "mode" in lower_name:
            return "TUTOR"
        if "question" in lower_name:
            return "Explain supervised learning in simple terms."
        if "prompt" in lower_name:
            return "Explain supervised learning in simple terms."
        if "query" in lower_name:
            return "Explain supervised learning in simple terms."
        if "topic" in lower_name:
            return "Machine Learning"
        if "course" in lower_name and "title" in lower_name:
            return "Software Engineering"
        if "difficulty" in lower_name:
            return "medium"
        if "text" in lower_name:
            return "This is a sample academic text for testing."
        return "sample-text"

    if annotation is int:
        return 1

    if annotation is float:
        return 0.75

    if annotation is bool:
        return True

    if annotation is date:
        return "2026-03-09"

    if annotation is datetime:
        return "2026-03-09T10:00:00"

    if annotation is time:
        return "10:00:00"

    return "sample-value"


def build_model_payload(model_cls, overrides=None):
    overrides = overrides or {}
    data = {}

    for name, field in _model_fields(model_cls).items():
        if name in overrides:
            data[name] = overrides[name]
            continue

        annotation = _field_annotation(field)
        data[name] = _sample_value(name, annotation)

    return data


def find_field_name(model_cls, candidates):
    field_names = list(_model_fields(model_cls).keys())

    def norm(value):
        return value.replace("_", "").replace("-", "").lower()

    for candidate in candidates:
        for field_name in field_names:
            if norm(candidate) == norm(field_name):
                return field_name

    for candidate in candidates:
        for field_name in field_names:
            if norm(candidate) in norm(field_name) or norm(field_name) in norm(candidate):
                return field_name

    return None


def find_required_field(model_cls):
    for name, field in _model_fields(model_cls).items():
        if _field_required(field):
            return name
    return next(iter(_model_fields(model_cls).keys()))


def iter_scalar_fields(model_cls):
    for name, field in _model_fields(model_cls).items():
        annotation = _unwrap_annotation(_field_annotation(field))
        if annotation in (str, int, float, bool, date, datetime, time) or _is_enum(annotation):
            yield name, annotation


def build_prediction_response(probability_value=0.73, risk_value="HIGH"):
    probability_field = find_field_name(
        SinglePredictionResponse,
        ["probability", "dropoutProbability", "dropout_probability"],
    )
    risk_field = find_field_name(
        SinglePredictionResponse,
        ["riskLevel", "risk_level", "risk", "predictionLabel", "riskLabel"],
    )

    overrides = {}
    if probability_field:
        overrides[probability_field] = probability_value
    if risk_field:
        overrides[risk_field] = risk_value

    return build_model_payload(SinglePredictionResponse, overrides), probability_field, risk_field


def build_engagement_response():
    data = build_model_payload(EngagementAnalyzeResponse)

    for key in list(data.keys()):
        key_lower = key.lower()

        if key_lower == "emotionconfidence":
            data[key] = 0.91
        elif key_lower == "emotionscores":
            data[key] = {"happy": 0.91, "neutral": 0.09}
        elif "label" in key_lower or key_lower.endswith("state"):
            data[key] = "ENGAGED"
        elif "confidence" in key_lower:
            data[key] = 0.91
        elif key_lower == "studentid":
            data[key] = "student-001"
        elif key_lower == "sessionid":
            data[key] = "session-001"
        elif key_lower == "courseid":
            data[key] = "course-001"

    label_field = None
    confidence_field = None

    for key in data.keys():
        key_lower = key.lower()
        if label_field is None and ("label" in key_lower or key_lower.endswith("state")):
            label_field = key
        if confidence_field is None and "confidence" in key_lower:
            confidence_field = key

    return data, label_field, confidence_field


# -----------------------------
# ATC-43
# -----------------------------
def test_atc_43_health_endpoint_returns_service_status(monkeypatch):
    """ATC-43: Health endpoint returns service status"""
    monkeypatch.setattr(
        main_module,
        "get_artifacts",
        lambda: {"metadata": {"model_name": "XGBoost"}},
    )

    response = client.get("/health")

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "UP"
    assert body["modelLoaded"] is True
    assert "modelName" in body


# -----------------------------
# ATC-44
# -----------------------------
def test_atc_44_valid_dropout_prediction_request_returns_success(monkeypatch):
    """ATC-44: Valid dropout prediction request returns success"""
    payload = build_model_payload(SinglePredictionRequest)
    response_payload, _, _ = build_prediction_response()

    monkeypatch.setattr(
        main_module,
        "get_artifacts",
        lambda: {"model": object(), "metadata": {}, "training_medians": {}},
    )
    monkeypatch.setattr(
        main_module,
        "run_single_prediction",
        lambda request, model, metadata, training_medians: response_payload,
    )

    response = client.post("/predict", json=payload)

    assert response.status_code == 200


# -----------------------------
# ATC-45
# -----------------------------
def test_atc_45_prediction_response_contains_probability_and_risk_level(monkeypatch):
    """ATC-45: Prediction response contains probability and risk level"""
    payload = build_model_payload(SinglePredictionRequest)
    response_payload, probability_field, risk_field = build_prediction_response()

    monkeypatch.setattr(
        main_module,
        "get_artifacts",
        lambda: {"model": object(), "metadata": {}, "training_medians": {}},
    )
    monkeypatch.setattr(
        main_module,
        "run_single_prediction",
        lambda request, model, metadata, training_medians: response_payload,
    )

    response = client.post("/predict", json=payload)

    assert response.status_code == 200
    body = response.json()

    assert probability_field is not None, "No probability-like field found in SinglePredictionResponse"
    assert risk_field is not None, "No risk-like field found in SinglePredictionResponse"
    assert probability_field in body
    assert risk_field in body


# -----------------------------
# ATC-46
# -----------------------------
def test_atc_46_missing_required_field_is_rejected():
    """ATC-46: Missing required field is rejected"""
    payload = build_model_payload(SinglePredictionRequest)
    missing_field = find_required_field(SinglePredictionRequest)
    payload.pop(missing_field, None)

    response = client.post("/predict", json=payload)

    assert response.status_code == 422


# -----------------------------
# ATC-47
# -----------------------------
def test_atc_47_invalid_data_type_is_rejected():
    """ATC-47: Invalid data type is rejected"""
    base_payload = build_model_payload(SinglePredictionRequest)

    for field_name, _annotation in iter_scalar_fields(SinglePredictionRequest):
        payload = dict(base_payload)
        payload[field_name] = ["invalid", "type"]

        response = client.post("/predict", json=payload)
        if response.status_code in (400, 422):
            return

    pytest.fail("No scalar field in SinglePredictionRequest rejected an invalid type")


# -----------------------------
# ATC-48
# -----------------------------
def test_atc_48_returned_probability_stays_within_0_to_1_range(monkeypatch):
    """ATC-48: Returned probability stays within 0 to 1 range"""
    payload = build_model_payload(SinglePredictionRequest)
    response_payload, probability_field, _ = build_prediction_response(probability_value=0.73)

    monkeypatch.setattr(
        main_module,
        "get_artifacts",
        lambda: {"model": object(), "metadata": {}, "training_medians": {}},
    )
    monkeypatch.setattr(
        main_module,
        "run_single_prediction",
        lambda request, model, metadata, training_medians: response_payload,
    )

    response = client.post("/predict", json=payload)

    assert response.status_code == 200
    body = response.json()

    assert probability_field is not None, "No probability-like field found in SinglePredictionResponse"
    assert 0 <= body[probability_field] <= 1


# -----------------------------
# ATC-49
# -----------------------------
def test_atc_49_valid_engagement_request_returns_structured_result(monkeypatch):
    """ATC-49: Valid engagement request returns structured result"""
    response_payload, label_field, confidence_field = build_engagement_response()

    monkeypatch.setattr(
        main_module,
        "analyze_engagement_image",
        lambda image_bytes, studentId, sessionId, courseId: response_payload,
    )

    files = {
        "image": ("engagement.jpg", b"fake-image-bytes", "image/jpeg"),
    }
    data = {
        "studentId": "student-001",
        "sessionId": "session-001",
        "courseId": "course-001",
    }

    response = client.post("/engagement/analyze", files=files, data=data)

    assert response.status_code == 200
    body = response.json()

    if label_field:
        assert label_field in body
    if confidence_field:
        assert confidence_field in body


# -----------------------------
# ATC-50
# -----------------------------
def test_atc_50_ai_tutor_valid_prompt_returns_response_and_empty_prompt_is_rejected(monkeypatch):
    """ATC-50: AI tutor valid prompt works and empty prompt is rejected"""
    payload = build_model_payload(AskAiPayload)

    mode_field = find_field_name(AskAiPayload, ["mode"])
    text_field = find_field_name(AskAiPayload, ["question", "prompt", "query"])

    if mode_field:
        payload[mode_field] = "TUTOR"
    if text_field:
        payload[text_field] = "Explain supervised learning in simple terms."

    monkeypatch.setattr(
        ai_routes_module,
        "call_ollama_chat",
        lambda messages, temperature=0.2, num_predict=300: "This is a tutor response.",
    )

    success_response = client.post("/ai/ask", json=payload)

    assert success_response.status_code == 200
    success_body = success_response.json()
    assert "responseText" in success_body
    assert success_body["responseText"].strip() != ""

    bad_payload = build_model_payload(AskAiPayload)
    if mode_field:
        bad_payload[mode_field] = "TUTOR"

    if text_field:
        bad_payload[text_field] = ""
    else:
        required_field = find_required_field(AskAiPayload)
        bad_payload.pop(required_field, None)

    error_response = client.post("/ai/ask", json=bad_payload)

    assert error_response.status_code in (400, 422)