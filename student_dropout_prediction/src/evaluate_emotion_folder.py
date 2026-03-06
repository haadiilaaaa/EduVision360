from pathlib import Path
import json
import argparse
import random

import cv2
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from deepface import DeepFace
from sklearn.metrics import (
    accuracy_score,
    precision_recall_fscore_support,
    classification_report,
    confusion_matrix,
    ConfusionMatrixDisplay,
)
from sklearn.model_selection import train_test_split


CLASS_NAMES = ["angry", "disgust", "fear", "happy", "sad", "surprise", "neutral"]
IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}


def normalize_label(x: str) -> str:
    x = str(x).strip().lower()
    # common folder variants
    mapping = {
        "anger": "angry",
        "happiness": "happy",
        "sadness": "sad",
    }
    return mapping.get(x, x)


def predict_emotion(face_bgr: np.ndarray, detector_backend: str):
    """
    Returns:
      dominant_emotion (str), confidence(0..1), scores dict
    """
    result = DeepFace.analyze(
        img_path=face_bgr,
        actions=["emotion"],
        detector_backend=detector_backend,   # "skip" if already cropped, else "opencv"
        enforce_detection=False
    )

    if isinstance(result, list):
        result = result[0] if result else {}

    dominant = normalize_label(result.get("dominant_emotion"))
    raw_scores = result.get("emotion") or {}

    scores = {}
    for k, v in raw_scores.items():
        key = normalize_label(k)
        try:
            scores[key] = float(v) / 100.0
        except Exception:
            scores[key] = 0.0

    conf = scores.get(dominant, 0.0) if dominant else 0.0
    return dominant, conf, scores


def save_confusion_matrix(cm, out_path: Path, labels, normalized: bool = False):
    fig, ax = plt.subplots(figsize=(8, 6))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=labels)
    disp.plot(ax=ax, values_format=".2f" if normalized else "d")
    ax.set_title("Normalized Confusion Matrix" if normalized else "Confusion Matrix")
    plt.tight_layout()
    plt.savefig(out_path, dpi=220, bbox_inches="tight")
    plt.close(fig)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--data-dir", default="data/raw/fer_images", help="Folder containing train/ and test/")
    parser.add_argument("--split", default="test", choices=["train", "test"], help="Which split to evaluate")
    parser.add_argument("--sample-size", type=int, default=300, help="How many images to evaluate")
    parser.add_argument("--seed", type=int, default=42, help="Random seed")
    parser.add_argument("--out-dir", default="emotion_reports", help="Output folder")
    parser.add_argument("--detector-backend", default="skip", help='DeepFace detector backend: "skip" (cropped) or "opencv"')
    args = parser.parse_args()

    random.seed(args.seed)
    np.random.seed(args.seed)

    data_dir = Path(args.data_dir)
    split_dir = data_dir / args.split
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    if not split_dir.exists():
        raise FileNotFoundError(f"Split folder not found: {split_dir}")

    # Collect (path, label) from split folder
    items = []
    for class_dir in split_dir.iterdir():
        if not class_dir.is_dir():
            continue
        label = normalize_label(class_dir.name)
        if label not in CLASS_NAMES:
            continue

        for p in class_dir.rglob("*"):
            if p.is_file() and p.suffix.lower() in IMG_EXTS:
                items.append((p, label))

    if not items:
        raise ValueError(
            f"No images found. Expected subfolders like {split_dir}/angry, {split_dir}/happy, etc."
        )

    df = pd.DataFrame(items, columns=["path", "true_label"])

    # stratified sampling if possible
    if args.sample_size and args.sample_size < len(df):
        try:
            sampled_df, _ = train_test_split(
                df,
                train_size=args.sample_size,
                stratify=df["true_label"],
                random_state=args.seed,
            )
            df = sampled_df.copy().reset_index(drop=True)
        except Exception:
            df = df.sample(n=args.sample_size, random_state=args.seed).reset_index(drop=True)

    print(f"Evaluating {len(df)} images from {split_dir} using detector_backend={args.detector_backend}")

    y_true = []
    y_pred = []
    rows = []

    for i, row in df.iterrows():
        img_path = Path(row["path"])
        true_label = row["true_label"]

        img = cv2.imread(str(img_path))
        if img is None:
            continue

        try:
            pred_label, pred_conf, score_map = predict_emotion(img, args.detector_backend)
        except Exception as e:
            print(f"[WARN] Failed on {img_path.name}: {e}")
            pred_label, pred_conf, score_map = None, 0.0, {}

        pred_label = normalize_label(pred_label) if pred_label else "neutral"
        if pred_label not in CLASS_NAMES:
            pred_label = "neutral"

        y_true.append(true_label)
        y_pred.append(pred_label)

        rows.append({
            "path": str(img_path),
            "true_label": true_label,
            "predicted_label": pred_label,
            "confidence": round(float(pred_conf), 4),
            "score_angry": round(float(score_map.get("angry", 0.0)), 4),
            "score_disgust": round(float(score_map.get("disgust", 0.0)), 4),
            "score_fear": round(float(score_map.get("fear", 0.0)), 4),
            "score_happy": round(float(score_map.get("happy", 0.0)), 4),
            "score_sad": round(float(score_map.get("sad", 0.0)), 4),
            "score_surprise": round(float(score_map.get("surprise", 0.0)), 4),
            "score_neutral": round(float(score_map.get("neutral", 0.0)), 4),
        })

        if (i + 1) % 25 == 0:
            print(f"Processed {i + 1}/{len(df)}")

    if not y_true:
        raise ValueError("No valid predictions were generated. Check your images and detector backend.")

    accuracy = accuracy_score(y_true, y_pred)
    precision_macro, recall_macro, f1_macro, _ = precision_recall_fscore_support(
        y_true, y_pred, average="macro", zero_division=0
    )
    precision_weighted, recall_weighted, f1_weighted, _ = precision_recall_fscore_support(
        y_true, y_pred, average="weighted", zero_division=0
    )

    report_dict = classification_report(
        y_true, y_pred, labels=CLASS_NAMES, output_dict=True, zero_division=0
    )
    report_df = pd.DataFrame(report_dict).transpose()

    cm = confusion_matrix(y_true, y_pred, labels=CLASS_NAMES)
    cm_norm = cm.astype(float)
    row_sums = cm_norm.sum(axis=1, keepdims=True)
    cm_norm = np.divide(cm_norm, row_sums, out=np.zeros_like(cm_norm), where=row_sums != 0)

    metrics = {
        "dataset_format": "folder",
        "dataset_root": str(data_dir),
        "split": args.split,
        "rows_evaluated": int(len(y_true)),
        "accuracy": float(accuracy),
        "precision_macro": float(precision_macro),
        "recall_macro": float(recall_macro),
        "f1_macro": float(f1_macro),
        "precision_weighted": float(precision_weighted),
        "recall_weighted": float(recall_weighted),
        "f1_weighted": float(f1_weighted),
        "model_type": "DeepFace pretrained emotion inference",
        "detector_backend": args.detector_backend,
        "notes": "Inference-only benchmark on FER-style folder dataset; no custom training performed."
    }

    (out_dir / "emotion_reports").mkdir(parents=True, exist_ok=True)
    out_dir = out_dir / "emotion_reports"

    with open(out_dir / "emotion_metrics.json", "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2)

    pd.DataFrame([metrics]).to_csv(out_dir / "emotion_metrics_table.csv", index=False)
    report_df.to_csv(out_dir / "emotion_classification_report.csv")
    pd.DataFrame(rows).to_csv(out_dir / "emotion_predictions_sample.csv", index=False)

    save_confusion_matrix(cm, out_dir / "emotion_confusion_matrix.png", CLASS_NAMES, normalized=False)
    save_confusion_matrix(cm_norm, out_dir / "emotion_confusion_matrix_normalized.png", CLASS_NAMES, normalized=True)

    print("\n✅ Emotion evidence pack generated successfully")
    print(f"Output folder: {out_dir.resolve()}")
    print("Files created:")
    print("- emotion_metrics.json")
    print("- emotion_metrics_table.csv")
    print("- emotion_classification_report.csv")
    print("- emotion_predictions_sample.csv")
    print("- emotion_confusion_matrix.png")
    print("- emotion_confusion_matrix_normalized.png")


if __name__ == "__main__":
    main()