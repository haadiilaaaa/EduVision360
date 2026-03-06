from typing import Any, Dict, List, Optional
from pydantic import BaseModel, Field


class SinglePredictionRequest(BaseModel):
    studentId: Optional[str] = None
    studentName: Optional[str] = None
    courseId: Optional[str] = None
    features: Dict[str, Any] = Field(
        ...,
        description="Map of feature name to value. Use exact training column names."
    )


class SinglePredictionResponse(BaseModel):
    studentId: Optional[str] = None
    studentName: Optional[str] = None
    courseId: Optional[str] = None
    dropoutProbability: float
    predictedLabel: str
    riskLevel: str
    threshold: float


class BatchPredictionRequest(BaseModel):
    students: List[SinglePredictionRequest]


class BatchPredictionResponse(BaseModel):
    totalStudents: int
    predictions: List[SinglePredictionResponse]


class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
    modelName: str


class ModelInfoResponse(BaseModel):
    modelName: str
    threshold: float
    featureCount: int
    featureColumns: List[str]