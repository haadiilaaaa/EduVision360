from datetime import datetime, timezone

import cv2
import numpy as np

from .engagement_schemas import EngagementAnalyzeResponse
from .emotion_service import analyze_face_emotion, get_emotion_score_adjustment


FACE_CASCADE = cv2.CascadeClassifier(
    cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
)
EYE_CASCADE = cv2.CascadeClassifier(
    cv2.data.haarcascades + "haarcascade_eye.xml"
)


def _decode_image(image_bytes: bytes):
    np_arr = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    return image


def _clip_score(value: float) -> float:
    return float(max(0.0, min(100.0, value)))


def _confidence_from_score(score: float, label: str) -> float:
    if label == "ATTENTIVE":
        conf = 0.65 + ((score - 70.0) / 100.0)
    elif label == "DISTRACTED":
        conf = 0.65 + ((40.0 - score) / 100.0)
    else:
        conf = 0.68 + (abs(score - 55.0) / 200.0)

    return float(round(max(0.55, min(0.95, conf)), 4))


def analyze_engagement_image(
    image_bytes: bytes,
    studentId: str | None = None,
    sessionId: str | None = None,
    courseId: str | None = None,
) -> EngagementAnalyzeResponse:
    image = _decode_image(image_bytes)
    if image is None:
        raise ValueError("Could not decode uploaded image")

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    img_h, img_w = gray.shape[:2]

    faces = FACE_CASCADE.detectMultiScale(
        gray,
        scaleFactor=1.1,
        minNeighbors=5,
        minSize=(80, 80)
    )

    face_count = 0 if faces is None else len(faces)

    if face_count == 0:
        return EngagementAnalyzeResponse(
            studentId=studentId,
            sessionId=sessionId,
            courseId=courseId,
            label="DISTRACTED",
            confidence=0.78,
            engagementScore=20.0,
            faceDetected=False,
            faceCount=0,
            centeredFace=False,
            eyesDetected=False,
            lookingAway=True,
            dominantEmotion=None,
            emotionConfidence=None,
            emotionScores={},
            modelType="PRETRAINED_HYBRID",
            message="No clear face detected in the frame",
            capturedAt=datetime.now(timezone.utc)
        )

    # use largest face
    x, y, w, h = max(faces, key=lambda f: f[2] * f[3])

    roi_gray = gray[y:y + h, x:x + w]
    roi_bgr = image[y:y + h, x:x + w]

    eyes = EYE_CASCADE.detectMultiScale(
        roi_gray,
        scaleFactor=1.1,
        minNeighbors=4,
        minSize=(18, 18)
    )

    eyes_detected = len(eyes) >= 1

    face_cx = x + (w / 2.0)
    face_cy = y + (h / 2.0)
    img_cx = img_w / 2.0
    img_cy = img_h / 2.0

    dx = abs(face_cx - img_cx) / max(img_cx, 1.0)
    dy = abs(face_cy - img_cy) / max(img_cy, 1.0)

    centered_face = dx <= 0.35 and dy <= 0.35
    looking_away = dx > 0.40

    face_area_ratio = (w * h) / float(max(img_w * img_h, 1))
    brightness = float(np.mean(roi_gray)) if roi_gray.size > 0 else 0.0

    # ---------- base engagement score from visual attention cues ----------
    score = 0.0

    # face exists
    score += 35.0

    # centered in frame
    if centered_face:
        score += 25.0
    else:
        score -= 10.0

    # likely not looking away
    if not looking_away:
        score += 15.0
    else:
        score -= 15.0

    # eye presence
    if eyes_detected:
        score += 15.0
    else:
        score -= 10.0

    # face visibility / size
    if face_area_ratio >= 0.08:
        score += 10.0
    elif face_area_ratio >= 0.04:
        score += 5.0
    else:
        score -= 10.0

    # lighting
    if brightness >= 65:
        score += 10.0
    elif brightness >= 40:
        score += 5.0
    else:
        score -= 5.0

    # multiple faces penalty
    if face_count > 1:
        score -= 10.0

    # ---------- pretrained emotion inference ----------
    dominant_emotion, emotion_confidence, emotion_scores = analyze_face_emotion(roi_bgr)

    # small fusion adjustment
    score += get_emotion_score_adjustment(dominant_emotion)

    score = _clip_score(score)

    if score >= 70:
        label = "ATTENTIVE"
        message = "Student appears attentive in the current frame"
    elif score >= 40:
        label = "NEUTRAL"
        message = "Student appears moderately engaged"
    else:
        label = "DISTRACTED"
        message = "Student may be distracted or not properly visible"

    if dominant_emotion:
        message += f"; dominant emotion detected: {dominant_emotion}"

    confidence = _confidence_from_score(score, label)

    return EngagementAnalyzeResponse(
        studentId=studentId,
        sessionId=sessionId,
        courseId=courseId,
        label=label,
        confidence=round(confidence, 4),
        engagementScore=round(score, 2),
        faceDetected=True,
        faceCount=face_count,
        centeredFace=centered_face,
        eyesDetected=eyes_detected,
        lookingAway=looking_away,
        dominantEmotion=dominant_emotion,
        emotionConfidence=round(emotion_confidence, 4) if emotion_confidence is not None else None,
        emotionScores=emotion_scores,
        modelType="PRETRAINED_HYBRID",
        message=message,
        capturedAt=datetime.now(timezone.utc)
    )