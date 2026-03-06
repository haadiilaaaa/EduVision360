from typing import Optional

from deepface import DeepFace


def analyze_face_emotion(face_bgr) -> tuple[Optional[str], Optional[float], dict[str, float]]:
    """
    Returns:
      dominant_emotion: lowercase string like happy / neutral / sad / angry ...
      emotion_confidence: 0.0 - 1.0
      emotion_scores: dict of emotion -> probability-ish score (0.0 - 1.0)
    """
    try:
        result = DeepFace.analyze(
            img_path=face_bgr,
            actions=["emotion"],
            detector_backend="opencv",
            enforce_detection=False
        )

        if isinstance(result, list):
            result = result[0] if result else {}

        dominant_emotion = result.get("dominant_emotion")
        if dominant_emotion is not None:
            dominant_emotion = str(dominant_emotion).lower()

        raw_scores = result.get("emotion") or {}
        emotion_scores = {}

        for key, value in raw_scores.items():
            try:
                emotion_scores[str(key).lower()] = round(float(value) / 100.0, 4)
            except Exception:
                pass

        emotion_confidence = None
        if dominant_emotion and dominant_emotion in emotion_scores:
            emotion_confidence = emotion_scores[dominant_emotion]

        return dominant_emotion, emotion_confidence, emotion_scores

    except Exception as e:
        print(f"[emotion_service] emotion analysis failed: {e}")
        return None, None, {}


def get_emotion_score_adjustment(dominant_emotion: Optional[str]) -> float:
    """
    Small fusion layer:
    - positive / calm emotions slightly raise engagement
    - clearly negative emotions slightly lower engagement
    """
    if dominant_emotion == "happy":
        return 7.0
    if dominant_emotion == "neutral":
        return 4.0
    if dominant_emotion == "surprise":
        return 2.0

    if dominant_emotion in {"sad", "angry", "fear", "disgust"}:
        return -6.0

    return 0.0