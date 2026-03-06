from pathlib import Path
import json
import argparse

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import tensorflow as tf

from sklearn.metrics import (
    accuracy_score,
    precision_recall_fscore_support,
    classification_report,
    confusion_matrix,
    ConfusionMatrixDisplay,
)

IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}


def count_images_per_class(split_dir: Path, class_names):
    counts = {}
    for idx, class_name in enumerate(class_names):
        class_dir = split_dir / class_name
        count = 0
        if class_dir.exists():
            for p in class_dir.rglob("*"):
                if p.is_file() and p.suffix.lower() in IMG_EXTS:
                    count += 1
        counts[idx] = count
    return counts


def make_class_weights(train_dir: Path, class_names):
    counts = count_images_per_class(train_dir, class_names)
    total = sum(counts.values())
    num_classes = len(class_names)

    weights = {}
    for idx, count in counts.items():
        weights[idx] = total / (num_classes * max(count, 1))

    return weights


def save_training_curves(history, out_path: Path):
    hist = history.history

    fig, ax = plt.subplots(figsize=(8, 5))
    ax.plot(hist.get("accuracy", []), label="train_acc")
    ax.plot(hist.get("val_accuracy", []), label="val_acc")
    ax.set_title("Training Accuracy")
    ax.set_xlabel("Epoch")
    ax.set_ylabel("Accuracy")
    ax.legend()
    plt.tight_layout()
    plt.savefig(out_path / "training_accuracy.png", dpi=220, bbox_inches="tight")
    plt.close(fig)

    fig, ax = plt.subplots(figsize=(8, 5))
    ax.plot(hist.get("loss", []), label="train_loss")
    ax.plot(hist.get("val_loss", []), label="val_loss")
    ax.set_title("Training Loss")
    ax.set_xlabel("Epoch")
    ax.set_ylabel("Loss")
    ax.legend()
    plt.tight_layout()
    plt.savefig(out_path / "training_loss.png", dpi=220, bbox_inches="tight")
    plt.close(fig)


def save_confusion_matrix(cm, labels, out_file: Path, normalized=False):
    fig, ax = plt.subplots(figsize=(8, 6))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=labels)
    disp.plot(ax=ax, values_format=".2f" if normalized else "d")
    ax.set_title("Normalized Confusion Matrix" if normalized else "Confusion Matrix")
    plt.tight_layout()
    plt.savefig(out_file, dpi=220, bbox_inches="tight")
    plt.close(fig)


def build_model(img_size, num_classes):
    inputs = tf.keras.Input(shape=(img_size, img_size, 3))

    augmentation = tf.keras.Sequential([
        tf.keras.layers.RandomFlip("horizontal"),
        tf.keras.layers.RandomRotation(0.08),
        tf.keras.layers.RandomZoom(0.10),
        tf.keras.layers.RandomContrast(0.10),
    ], name="augmentation")

    x = augmentation(inputs)

    # EfficientNetB0 transfer learning
    base_model = tf.keras.applications.EfficientNetB0(
        include_top=False,
        weights="imagenet",
        input_shape=(img_size, img_size, 3)
    )
    base_model.trainable = False

    x = tf.keras.applications.efficientnet.preprocess_input(x)
    x = base_model(x, training=False)
    x = tf.keras.layers.GlobalAveragePooling2D()(x)
    x = tf.keras.layers.BatchNormalization()(x)
    x = tf.keras.layers.Dropout(0.35)(x)
    x = tf.keras.layers.Dense(256, activation="relu")(x)
    x = tf.keras.layers.BatchNormalization()(x)
    x = tf.keras.layers.Dropout(0.25)(x)
    outputs = tf.keras.layers.Dense(num_classes, activation="softmax")(x)

    model = tf.keras.Model(inputs, outputs)
    return model, base_model


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--data-dir", default="data/raw/fer_images", help="Root folder containing train/ and test/")
    parser.add_argument("--img-size", type=int, default=96, help="Image size for training")
    parser.add_argument("--batch-size", type=int, default=32, help="Batch size")
    parser.add_argument("--val-split", type=float, default=0.2, help="Validation split from train folder")
    parser.add_argument("--seed", type=int, default=42, help="Random seed")
    parser.add_argument("--head-epochs", type=int, default=12, help="Epochs with frozen base model")
    parser.add_argument("--finetune-epochs", type=int, default=10, help="Epochs after unfreezing top layers")
    parser.add_argument("--out-dir", default="emotion_reports/trained_model", help="Output folder")
    args = parser.parse_args()

    tf.keras.utils.set_random_seed(args.seed)
    AUTOTUNE = tf.data.AUTOTUNE

    data_dir = Path(args.data_dir)
    train_dir = data_dir / "train"
    test_dir = data_dir / "test"
    out_dir = Path(args.out_dir)
    model_dir = Path("models/emotion")
    out_dir.mkdir(parents=True, exist_ok=True)
    model_dir.mkdir(parents=True, exist_ok=True)

    if not train_dir.exists():
        raise FileNotFoundError(f"Train folder not found: {train_dir}")
    if not test_dir.exists():
        raise FileNotFoundError(f"Test folder not found: {test_dir}")

    print("Loading datasets...")

    train_ds = tf.keras.utils.image_dataset_from_directory(
        train_dir,
        labels="inferred",
        label_mode="int",
        validation_split=args.val_split,
        subset="training",
        seed=args.seed,
        image_size=(args.img_size, args.img_size),
        batch_size=args.batch_size,
        color_mode="rgb"
    )

    val_ds = tf.keras.utils.image_dataset_from_directory(
        train_dir,
        labels="inferred",
        label_mode="int",
        validation_split=args.val_split,
        subset="validation",
        seed=args.seed,
        image_size=(args.img_size, args.img_size),
        batch_size=args.batch_size,
        color_mode="rgb"
    )

    test_ds = tf.keras.utils.image_dataset_from_directory(
        test_dir,
        labels="inferred",
        label_mode="int",
        seed=args.seed,
        image_size=(args.img_size, args.img_size),
        batch_size=args.batch_size,
        color_mode="rgb",
        shuffle=False
    )

    class_names = train_ds.class_names
    num_classes = len(class_names)

    print(f"Classes: {class_names}")

    train_ds = train_ds.prefetch(AUTOTUNE)
    val_ds = val_ds.prefetch(AUTOTUNE)
    test_ds = test_ds.prefetch(AUTOTUNE)

    class_weights = make_class_weights(train_dir, class_names)
    print("Class weights:", class_weights)

    model, base_model = build_model(args.img_size, num_classes)

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )

    checkpoint_path = model_dir / "best_emotion_model.weights.h5"

    callbacks = [
        tf.keras.callbacks.ModelCheckpoint(
            filepath=str(checkpoint_path),
            monitor="val_accuracy",
            save_best_only=True,
            save_weights_only=True,
            mode="max",
            verbose=1
        ),
        tf.keras.callbacks.EarlyStopping(
            monitor="val_accuracy",
            patience=5,
            mode="max",
            restore_best_weights=True,
            verbose=1
        ),
        tf.keras.callbacks.ReduceLROnPlateau(
            monitor="val_loss",
            factor=0.3,
            patience=2,
            min_lr=1e-6,
            verbose=1
        ),
        tf.keras.callbacks.CSVLogger(str(out_dir / "training_log.csv"))
    ]

    print("\nTraining head...")
    history_1 = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=args.head_epochs,
        class_weight=class_weights,
        callbacks=callbacks,
        verbose=1
    )

    print("\nFine-tuning top layers...")
    base_model.trainable = True

    # freeze most of the base model, unfreeze top part
    for layer in base_model.layers[:-40]:
        layer.trainable = False

    for layer in base_model.layers:
        if isinstance(layer, tf.keras.layers.BatchNormalization):
            layer.trainable = False

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )

    history_2 = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=args.finetune_epochs,
        class_weight=class_weights,
        callbacks=callbacks,
        verbose=1
    )

    print("\nLoading best saved weights...")
    model.load_weights(checkpoint_path)
    best_model = model

    print("\nEvaluating on test set...")
    y_true = []
    for _, labels in test_ds:
        y_true.extend(labels.numpy().tolist())
    y_true = np.array(y_true)

    probs = best_model.predict(test_ds, verbose=1)
    y_pred = np.argmax(probs, axis=1)

    accuracy = accuracy_score(y_true, y_pred)
    precision_macro, recall_macro, f1_macro, _ = precision_recall_fscore_support(
        y_true, y_pred, average="macro", zero_division=0
    )
    precision_weighted, recall_weighted, f1_weighted, _ = precision_recall_fscore_support(
        y_true, y_pred, average="weighted", zero_division=0
    )

    report_dict = classification_report(
        y_true,
        y_pred,
        target_names=class_names,
        output_dict=True,
        zero_division=0
    )
    report_df = pd.DataFrame(report_dict).transpose()

    cm = confusion_matrix(y_true, y_pred)
    cm_norm = cm.astype(float)
    row_sums = cm_norm.sum(axis=1, keepdims=True)
    cm_norm = np.divide(cm_norm, row_sums, out=np.zeros_like(cm_norm), where=row_sums != 0)

    metrics = {
        "dataset_format": "folder",
        "dataset_root": str(data_dir),
        "train_split": "train",
        "test_split": "test",
        "rows_evaluated": int(len(y_true)),
        "accuracy": float(accuracy),
        "precision_macro": float(precision_macro),
        "recall_macro": float(recall_macro),
        "f1_macro": float(f1_macro),
        "precision_weighted": float(precision_weighted),
        "recall_weighted": float(recall_weighted),
        "f1_weighted": float(f1_weighted),
        "model_type": "EfficientNetB0 transfer learning",
        "img_size": args.img_size,
        "batch_size": args.batch_size,
        "head_epochs": args.head_epochs,
        "finetune_epochs": args.finetune_epochs,
        "class_names": class_names
    }

    with open(out_dir / "trained_emotion_metrics.json", "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2)

    pd.DataFrame([metrics]).to_csv(out_dir / "trained_emotion_metrics_table.csv", index=False)
    report_df.to_csv(out_dir / "trained_emotion_classification_report.csv")

    pred_rows = []
    file_paths = test_ds.file_paths if hasattr(test_ds, "file_paths") else []
    for i in range(len(y_true)):
        pred_rows.append({
            "path": file_paths[i] if i < len(file_paths) else "",
            "true_label": class_names[y_true[i]],
            "predicted_label": class_names[y_pred[i]],
            "confidence": round(float(np.max(probs[i])), 4)
        })
    pd.DataFrame(pred_rows).to_csv(out_dir / "trained_emotion_predictions.csv", index=False)

    save_confusion_matrix(cm, class_names, out_dir / "trained_emotion_confusion_matrix.png", normalized=False)
    save_confusion_matrix(cm_norm, class_names, out_dir / "trained_emotion_confusion_matrix_normalized.png", normalized=True)

    # merge histories for simple plots
    merged_history = tf.keras.callbacks.History()
    merged_history.history = {}
    for key in set(history_1.history.keys()).union(history_2.history.keys()):
        merged_history.history[key] = history_1.history.get(key, []) + history_2.history.get(key, [])
    save_training_curves(merged_history, out_dir)

    # save metadata for later app integration
    model_metadata = {
        "model_name": "EfficientNetB0_FER",
        "class_names": class_names,
        "img_size": args.img_size
    }
    with open(model_dir / "best_emotion_model_metadata.json", "w", encoding="utf-8") as f:
        json.dump(model_metadata, f, indent=2)

    print("\n✅ Training and evaluation complete")
    print(f"Best model saved to: {checkpoint_path.resolve()}")
    print(f"Reports saved to: {out_dir.resolve()}")


if __name__ == "__main__":
    main()