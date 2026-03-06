from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware

from .model_loader import get_artifacts
from .predictor import run_single_prediction, run_batch_prediction
from .behavior_predictor import is_behavior_feature_request, run_behavior_prediction
from .schemas import (
    BatchPredictionRequest,
    BatchPredictionResponse,
    HealthResponse,
    ModelInfoResponse,
    SinglePredictionRequest,
    SinglePredictionResponse
)
from .ai_routes import router as ai_router
from .engagement_schemas import EngagementAnalyzeResponse
from .engagement_service import analyze_engagement_image

app = FastAPI(
    title="EduVision360 AI Service",
    version="1.2.0",
    description="FastAPI service for dropout prediction, AI tutor, and engagement analysis."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # development only
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(ai_router)


@app.get("/")
def root():
    return {
        "message": "EduVision360 AI Service is running"
    }


@app.get("/health", response_model=HealthResponse)
def health():
    try:
        artifacts = get_artifacts()
        metadata = artifacts["metadata"]

        return HealthResponse(
            status="UP",
            modelLoaded=True,
            modelName=metadata.get("model_name", "XGBoost")
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/model-info", response_model=ModelInfoResponse)
def model_info():
    try:
        artifacts = get_artifacts()
        metadata = artifacts["metadata"]

        return ModelInfoResponse(
            modelName=metadata.get("model_name", "XGBoost"),
            threshold=float(metadata.get("threshold", 0.50)),
            featureCount=len(metadata["feature_columns"]),
            featureColumns=metadata["feature_columns"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/predict", response_model=SinglePredictionResponse)
def predict(request: SinglePredictionRequest):
    try:
        if is_behavior_feature_request(request):
            return run_behavior_prediction(request)

        artifacts = get_artifacts()

        return run_single_prediction(
            request=request,
            model=artifacts["model"],
            metadata=artifacts["metadata"],
            training_medians=artifacts["training_medians"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/predict-batch", response_model=BatchPredictionResponse)
def predict_batch(request: BatchPredictionRequest):
    try:
        behavior_requests = []
        normal_requests = []

        for student in request.students:
            if is_behavior_feature_request(student):
                behavior_requests.append(student)
            else:
                normal_requests.append(student)

        predictions = []

        for student in behavior_requests:
            predictions.append(run_behavior_prediction(student))

        if normal_requests:
            artifacts = get_artifacts()
            predictions.extend(
                run_batch_prediction(
                    requests=normal_requests,
                    model=artifacts["model"],
                    metadata=artifacts["metadata"],
                    training_medians=artifacts["training_medians"]
                )
            )

        return BatchPredictionResponse(
            totalStudents=len(predictions),
            predictions=predictions
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/engagement/analyze", response_model=EngagementAnalyzeResponse)
async def engagement_analyze(
    image: UploadFile = File(...),
    studentId: str | None = Form(None),
    sessionId: str | None = Form(None),
    courseId: str | None = Form(None)
):
    try:
        image_bytes = await image.read()

        return analyze_engagement_image(
            image_bytes=image_bytes,
            studentId=studentId,
            sessionId=sessionId,
            courseId=courseId
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    @app.post("/engagement/analyze", response_model=EngagementAnalyzeResponse)
    async def engagement_analyze(
            image: UploadFile = File(...),
            studentId: str | None = Form(None),
            sessionId: str | None = Form(None),
            courseId: str | None = Form(None)
    ):
        ...