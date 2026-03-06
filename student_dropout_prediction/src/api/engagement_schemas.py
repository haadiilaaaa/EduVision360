from datetime import datetime
from pydantic import BaseModel


class EngagementAnalyzeResponse(BaseModel):
    studentId: str | None = None
    sessionId: str | None = None
    courseId: str | None = None

    # engagement outputs
    label: str
    confidence: float
    engagementScore: float

    # visual signals
    faceDetected: bool
    faceCount: int
    centeredFace: bool
    eyesDetected: bool
    lookingAway: bool

    # emotion outputs
    dominantEmotion: str | None = None
    emotionConfidence: float | None = None
    emotionScores: dict[str, float] | None = None
    modelType: str = "PRETRAINED_HYBRID"

    message: str
    capturedAt: datetime