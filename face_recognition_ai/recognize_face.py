import cv2
import os
import numpy as np
from mtcnn import MTCNN
from keras_facenet import FaceNet
from scipy.spatial.distance import cosine

# Initialize models
detector = MTCNN()
embedder = FaceNet()

DATASET_DIR = "dataset"
THRESHOLD = 0.5


def extract_embedding(face):
    face = cv2.resize(face, (160, 160))
    face = face.astype("float32")
    mean, std = face.mean(), face.std()
    face = (face - mean) / std
    return embedder.embeddings([face])[0]


def load_registered_embeddings():
    database = {}

    for student_id in os.listdir(DATASET_DIR):
        student_path = os.path.join(DATASET_DIR, student_id)
        emb_file = os.path.join(student_path, "embeddings.npy")

        if os.path.isfile(emb_file):
            database[student_id] = np.load(emb_file)

    return database


def recognize_face(face, database):
    embedding = extract_embedding(face)

    best_match = None
    best_score = float("inf")

    for student_id, embeddings in database.items():
        for registered_emb in embeddings:
            dist = cosine(embedding, registered_emb)
            if dist < best_score:
                best_score = dist
                best_match = student_id

    if best_score < THRESHOLD:
        return best_match, best_score
    else:
        return None, best_score


def start_recognition():
    database = load_registered_embeddings()
    cap = cv2.VideoCapture(0)

    print("Face recognition started. Press Q to quit.")

    while True:
        ret, frame = cap.read()
        if not ret:
            continue

        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        faces = detector.detect_faces(rgb)

        if faces:
            x, y, w, h = faces[0]["box"]
            x, y = abs(x), abs(y)
            face = rgb[y:y + h, x:x + w]

            student_id, score = recognize_face(face, database)

            if student_id:
                label = f"ID: {student_id} ({score:.2f})"
                color = (0, 255, 0)
            else:
                label = "Unknown"
                color = (0, 0, 255)

            cv2.rectangle(frame, (x, y), (x + w, y + h), color, 2)
            cv2.putText(frame, label, (x, y - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.9, color, 2)

        cv2.imshow("Face Recognition", frame)

        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    start_recognition()
