import os
import uuid
import cv2
import numpy as np
from typing import List, Dict, Tuple, Optional, Any

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from mtcnn import MTCNN
from keras_facenet import FaceNet
from scipy.spatial.distance import cosine
import uvicorn


# =========================================================
# Configuration
# =========================================================
FACE_DATA_DIR = "face_data"
DEBUG_DIR = "debug_faces"

os.makedirs(FACE_DATA_DIR, exist_ok=True)
os.makedirs(DEBUG_DIR, exist_ok=True)

# Recognition thresholds
MATCH_THRESHOLD = 0.45
UNCERTAIN_THRESHOLD = 0.60

# Registration quality thresholds
DETECTION_CONFIDENCE_THRESHOLD = 0.90
MIN_FACE_SIZE = 80           # minimum width/height of cropped face
MIN_SHARPNESS = 60.0         # Laplacian variance threshold
MIN_VALID_SAMPLES = 3        # minimum number of good images required

detector = MTCNN()
embedder = FaceNet()

app = FastAPI(
    title="EduVision 360 - Face Recognition Service",
    version="2.0.0"
)


# =========================================================
# Utility functions
# =========================================================
def extract_embedding(face_rgb: np.ndarray) -> np.ndarray:
    face_rgb = cv2.resize(face_rgb, (160, 160))
    face_rgb = face_rgb.astype("float32")

    mean, std = face_rgb.mean(), face_rgb.std()
    if std == 0:
        std = 1.0

    face_rgb = (face_rgb - mean) / std
    embedding = embedder.embeddings([face_rgb])[0]
    return embedding


def load_database() -> Dict[str, np.ndarray]:
    database: Dict[str, np.ndarray] = {}

    for file_name in os.listdir(FACE_DATA_DIR):
        if file_name.endswith(".npy"):
            student_id = file_name.replace(".npy", "")
            path = os.path.join(FACE_DATA_DIR, file_name)
            database[student_id] = np.load(path)

    return database


def crop_face(rgb: np.ndarray, box: List[int]) -> Optional[np.ndarray]:
    h_img, w_img, _ = rgb.shape
    x, y, w, h = box

    x1 = max(0, x)
    y1 = max(0, y)
    x2 = min(w_img, x + w)
    y2 = min(h_img, y + h)

    if x2 <= x1 or y2 <= y1:
        return None

    face = rgb[y1:y2, x1:x2]
    if face.size == 0:
        return None

    return face


def face_sharpness(face_rgb: np.ndarray) -> float:
    gray = cv2.cvtColor(face_rgb, cv2.COLOR_RGB2GRAY)
    return float(cv2.Laplacian(gray, cv2.CV_64F).var())


def save_debug_image(prefix: str, image: np.ndarray) -> str:
    file_name = f"{prefix}_{uuid.uuid4().hex[:8]}.jpg"
    path = os.path.join(DEBUG_DIR, file_name)
    cv2.imwrite(path, image)
    return path


def validate_registration_face(
    rgb: np.ndarray,
    faces: List[Dict[str, Any]]
) -> Tuple[Optional[np.ndarray], Optional[str]]:
    """
    Strict validation for registration.
    Rejects:
    - no face
    - multiple faces
    - low detector confidence
    - very small face
    - blurry face
    """

    if not faces:
        return None, "No face detected"

    if len(faces) > 1:
        return None, "Multiple faces detected. Please upload an image with only one face."

    detected = faces[0]
    confidence = float(detected.get("confidence", 0.0))

    if confidence < DETECTION_CONFIDENCE_THRESHOLD:
        return None, f"Face detection confidence too low ({confidence:.2f})"

    box = detected.get("box")
    if not box or len(box) != 4:
        return None, "Invalid face bounding box"

    _, _, w, h = box
    if w < MIN_FACE_SIZE or h < MIN_FACE_SIZE:
        return None, f"Face is too small ({w}x{h}). Please upload a clearer, closer image."

    face = crop_face(rgb, box)
    if face is None:
        return None, "Invalid face crop"

    sharpness = face_sharpness(face)
    if sharpness < MIN_SHARPNESS:
        return None, f"Face is too blurry ({sharpness:.2f}). Please upload a clearer image."

    return face, None


def validate_verification_face(
    rgb: np.ndarray,
    faces: List[Dict[str, Any]]
) -> Tuple[Optional[np.ndarray], Optional[str]]:
    """
    Verification is also kept reasonably strict for cleaner demo behavior.
    """

    if not faces:
        return None, "No face detected"

    if len(faces) > 1:
        return None, "Multiple faces detected. Please provide an image with only one face."

    detected = faces[0]
    box = detected.get("box")
    if not box or len(box) != 4:
        return None, "Invalid face bounding box"

    face = crop_face(rgb, box)
    if face is None:
        return None, "Invalid face crop"

    return face, None


def recognize_face(face_rgb: np.ndarray, database: Dict[str, np.ndarray]) -> Dict[str, Any]:
    emb = extract_embedding(face_rgb)

    best_match = None
    best_distance = float("inf")

    for student_id, embeddings in database.items():
        if embeddings.ndim == 1:
            embeddings = np.expand_dims(embeddings, axis=0)

        for saved_emb in embeddings:
            dist = cosine(emb, saved_emb)
            if dist < best_distance:
                best_distance = dist
                best_match = student_id

    similarity = 1 - best_distance
    similarity = max(0.0, min(1.0, similarity))
    confidence = round(similarity * 100, 2)

    if best_distance <= MATCH_THRESHOLD:
        return {
            "status": "MATCH",
            "recognized": True,
            "studentId": best_match,
            "confidence": confidence,
            "message": "Face recognized successfully"
        }

    if best_distance <= UNCERTAIN_THRESHOLD:
        return {
            "status": "UNCERTAIN",
            "recognized": False,
            "studentId": None,
            "confidence": confidence,
            "message": "Low confidence match. Please retry with a clearer image."
        }

    return {
        "status": "NO_MATCH",
        "recognized": False,
        "studentId": None,
        "confidence": confidence,
        "message": "Face not recognized"
    }


# =========================================================
# Routes
# =========================================================
@app.get("/health")
async def health():
    return {
        "status": "ok",
        "service": "EduVision 360 Face Recognition Service",
        "version": "2.0.0"
    }


@app.post("/register-face")
async def register_face(
    student_id: str = Form(...),
    files: List[UploadFile] = File(...)
):
    if not student_id.strip():
        raise HTTPException(status_code=400, detail="student_id is required")

    if not files:
        raise HTTPException(status_code=400, detail="At least one image file is required")

    print(f"Register request for student_id = {student_id}")
    print(f"Number of uploaded files = {len(files)}")

    embeddings: List[np.ndarray] = []
    invalid_files: List[Dict[str, str]] = []
    valid_files: List[str] = []

    for index, file in enumerate(files, start=1):
        file_name = file.filename or f"file_{index}"
        contents = await file.read()

        print(f"Processing file {index}: {file_name}, bytes={len(contents)}")

        if not contents:
            invalid_files.append({
                "file": file_name,
                "reason": "Empty file"
            })
            continue

        img_array = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

        if img is None:
            print(f"Image decode failed for {file_name}")
            invalid_files.append({
                "file": file_name,
                "reason": "Invalid image file"
            })
            continue

        save_debug_image(f"register_input_{index}", img)

        rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        faces = detector.detect_faces(rgb)
        print(f"Faces detected in {file_name} = {len(faces)}")

        face, error = validate_registration_face(rgb, faces)

        if error:
            print(f"Validation failed for {file_name}: {error}")
            invalid_files.append({
                "file": file_name,
                "reason": error
            })
            continue

        try:
            face_bgr = cv2.cvtColor(face, cv2.COLOR_RGB2BGR)
            save_debug_image(f"register_crop_{index}", face_bgr)

            emb = extract_embedding(face)
            embeddings.append(emb)
            valid_files.append(file_name)

            print(f"Valid embeddings count = {len(embeddings)}")

        except Exception as e:
            print(f"Embedding extraction failed for {file_name}: {e}")
            invalid_files.append({
                "file": file_name,
                "reason": f"Embedding extraction failed: {str(e)}"
            })

    # Strict behavior:
    # If any uploaded sample is invalid, reject the whole registration
    if invalid_files:
        raise HTTPException(
            status_code=400,
            detail={
                "message": "Face registration failed because some uploaded samples are invalid.",
                "studentId": student_id,
                "validSamples": len(embeddings),
                "requiredMinimum": MIN_VALID_SAMPLES,
                "invalidFiles": invalid_files
            }
        )

    if len(embeddings) < MIN_VALID_SAMPLES:
        raise HTTPException(
            status_code=400,
            detail={
                "message": f"At least {MIN_VALID_SAMPLES} valid face samples are required.",
                "studentId": student_id,
                "validSamples": len(embeddings),
                "requiredMinimum": MIN_VALID_SAMPLES
            }
        )

    embeddings_np = np.array(embeddings)
    np.save(os.path.join(FACE_DATA_DIR, f"{student_id}.npy"), embeddings_np)

    print(f"Saved embeddings for {student_id}")

    return {
        "success": True,
        "studentId": student_id,
        "samplesSaved": len(embeddings),
        "validFiles": valid_files,
        "message": "Face registered successfully"
    }


@app.post("/verify-face")
async def verify_face(file: UploadFile = File(...)):
    file_name = file.filename or "uploaded_file"
    contents = await file.read()

    if not contents:
        return {
            "recognized": False,
            "studentId": None,
            "confidence": 0.0,
            "message": "Empty file",
            "status": "NO_MATCH"
        }

    img_array = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    if img is None:
        return {
            "recognized": False,
            "studentId": None,
            "confidence": 0.0,
            "message": "Invalid image",
            "status": "NO_MATCH"
        }

    save_debug_image("verify_input", img)

    rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    faces = detector.detect_faces(rgb)

    face, error = validate_verification_face(rgb, faces)
    if error:
        return {
            "recognized": False,
            "studentId": None,
            "confidence": 0.0,
            "message": error,
            "status": "NO_MATCH"
        }

    try:
        face_bgr = cv2.cvtColor(face, cv2.COLOR_RGB2BGR)
        save_debug_image("verify_crop", face_bgr)
    except Exception:
        pass

    database = load_database()
    if not database:
        return {
            "recognized": False,
            "studentId": None,
            "confidence": 0.0,
            "message": "No registered faces",
            "status": "NO_MATCH"
        }

    return recognize_face(face, database)


if __name__ == "__main__":
    uvicorn.run(
        "ai_service:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )