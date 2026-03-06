import cv2
from mtcnn import MTCNN

# Initialize detector
detector = MTCNN()

def detect_and_crop_face(image_path):
    # Load image
    img = cv2.imread(image_path)
    if img is None:
        raise ValueError("Image not found or cannot be loaded")

    # Convert BGR (OpenCV) to RGB
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    # Detect faces
    results = detector.detect_faces(img_rgb)

    if len(results) == 0:
        print("❌ No face detected")
        return None

    # Take the first detected face
    x, y, w, h = results[0]['box']

    # Fix negative values (important!)
    x, y = abs(x), abs(y)

    face = img_rgb[y:y+h, x:x+w]

    print("✅ Face detected")
    return face


if __name__ == "__main__":
    face = detect_and_crop_face("test.jpg")

    if face is not None:
        cv2.imshow("Detected Face", cv2.cvtColor(face, cv2.COLOR_RGB2BGR))
        cv2.waitKey(0)
        cv2.destroyAllWindows()
