import cv2
import os
import numpy as np
from mtcnn import MTCNN
from keras_facenet import FaceNet

# Initialize models
detector = MTCNN()
embedder = FaceNet()

def extract_embedding(face):
    face = cv2.resize(face, (160, 160))
    face = face.astype('float32')
    mean, std = face.mean(), face.std()
    face = (face - mean) / std
    embedding = embedder.embeddings([face])[0]
    return embedding

def register_student(student_id, samples=5):
    save_path = f"dataset/{student_id}"
    os.makedirs(save_path, exist_ok=True)

    cap = cv2.VideoCapture(0)
    embeddings = []

    print(f"Registering student {student_id}")
    print("Press SPACE to capture face | Press Q to quit")

    while len(embeddings) < samples:
        ret, frame = cap.read()
        if not ret:
            continue

        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        faces = detector.detect_faces(rgb)

        if faces:
            x, y, w, h = faces[0]['box']
            x, y = abs(x), abs(y)
            face = rgb[y:y+h, x:x+w]

            cv2.rectangle(frame, (x, y), (x+w, y+h), (0,255,0), 2)

        cv2.imshow("Register Face", frame)

        key = cv2.waitKey(1) & 0xFF

        if key == ord(' ') and faces:
            embedding = extract_embedding(face)
            embeddings.append(embedding)
            print(f"Captured sample {len(embeddings)}/{samples}")

        elif key == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

    embeddings = np.array(embeddings)
    np.save(f"{save_path}/embeddings.npy", embeddings)
    print("✅ Registration complete")

if __name__ == "__main__":
    student_id = input("Enter Student ID: ")
    register_student(student_id)
