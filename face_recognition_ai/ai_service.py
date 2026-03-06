import os
import cv2
import numpy as np
from typing import List

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from mtcnn import MTCNN
from keras_facenet import FaceNet
from scipy.spatial.distance import cosine
import uvicorn

FACE_DATA_DIR = "face_data"
DEBUG_DIR = "debug_faces"
os.makedirs(FACE_DATA_DIR, exist_ok=True)
os.makedirs(DEBUG_DIR, exist_ok=True)

MATCH_THRESHOLD = 0.45
UNCERTAIN_THRESHOLD = 0.60

detector = MTCNN()
embedder = FaceNet()

app = FastAPI(
    title="EduVision 360 - Face Recognition Service",
    version="1.0.0"
)


def extract_embedding(face):
    face = cv2.resize(face, (160, 160))
    face = face.astype("float32")
    mean, std = face.mean(), face.std()
    if std == 0:
        std = 1.0
    face = (face - mean) / std
    return embedder.embeddings([face])[0]


def load_database():
    database = {}
    for file in os.listdir(FACE_DATA_DIR):
        if file.endswith(".npy"):
            student_id = file.replace(".npy", "")
            database[student_id] = np.load(os.path.join(FACE_DATA_DIR, file))
    return database


def crop_face(rgb, box):
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


def get_best_face(rgb, faces):
    if not faces:
        return None

    # choose the largest detected face
    best = max(faces, key=lambda f: f["box"][2] * f["box"][3])
    return crop_face(rgb, best["box"])


def recognize_face(face, database):
    emb = extract_embedding(face)

    best_match = None
    best_distance = float("inf")

    for student_id, embeddings in database.items():
        for saved_emb in embeddings:
            dist = cosine(emb, saved_emb)
            if dist < best_distance:
                best_distance = dist
                best_match = student_id

    similarity = 1 - best_distance
    confidence = round(similarity * 100, 2)

    if best_distance <= MATCH_THRESHOLD:
        return {
            "status": "MATCH",
            "recognized": True,
            "studentId": best_match,
            "confidence": confidence,
            "message": "Face recognized successfully"
        }

    elif best_distance <= UNCERTAIN_THRESHOLD:
        return {
            "status": "UNCERTAIN",
            "recognized": False,
            "studentId": None,
            "confidence": confidence,
            "message": "Low confidence. Please retry."
        }

    return {
        "status": "NO_MATCH",
        "recognized": False,
        "studentId": None,
        "confidence": confidence,
        "message": "Face not recognized"
    }


@app.post("/register-face")
async def register_face(
    student_id: str = Form(...),
    files: List[UploadFile] = File(...)
):
    print(f"Register request for student_id = {student_id}")
    print(f"Number of uploaded files = {len(files)}")

    embeddings = []

    for index, file in enumerate(files, start=1):
        contents = await file.read()
        print(f"Processing file {index}: {file.filename}, bytes={len(contents)}")

        img_array = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

        if img is None:
            print("Image decode failed")
            continue

        # save incoming image for debugging
        cv2.imwrite(os.path.join(DEBUG_DIR, f"register_input_{index}.jpg"), img)

        rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        faces = detector.detect_faces(rgb)
        print(f"Faces detected = {len(faces)}")

        face = get_best_face(rgb, faces)
        if face is None:
            print("No valid face crop")
            continue

        # save cropped face for debugging
        face_bgr = cv2.cvtColor(face, cv2.COLOR_RGB2BGR)
        cv2.imwrite(os.path.join(DEBUG_DIR, f"register_crop_{index}.jpg"), face_bgr)

        try:
            emb = extract_embedding(face)
            embeddings.append(emb)
            print(f"Valid embeddings = {len(embeddings)}")
        except Exception as e:
            print(f"Embedding extraction failed: {e}")

    if len(embeddings) < 1:
        raise HTTPException(
            status_code=400,
            detail="At least 1 valid face sample is required"
        )

    embeddings = np.array(embeddings)
    np.save(os.path.join(FACE_DATA_DIR, f"{student_id}.npy"), embeddings)

    print(f"Saved embeddings for {student_id}")

    return {
        "success": True,
        "studentId": student_id,
        "samplesSaved": len(embeddings),
        "message": "Face registered successfully"
    }


@app.post("/verify-face")
async def verify_face(file: UploadFile = File(...)):
    contents = await file.read()
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

    rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    faces = detector.detect_faces(rgb)

    face = get_best_face(rgb, faces)
    if face is None:
        return {
            "recognized": False,
            "studentId": None,
            "confidence": 0.0,
            "message": "No face detected",
            "status": "NO_MATCH"
        }

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