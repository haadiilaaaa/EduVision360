from pathlib import Path
import json
from datetime import datetime

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    ConfusionMatrixDisplay,
    classification_report,
    RocCurveDisplay,
    PrecisionRecallDisplay,
    average_precision_score,
    matthews_corrcoef,
    brier_score_loss,
)

# Your project structure imports
from .api.model_loader import get_artifacts
from .api.preprocess import prepare_dataframe


def _save_metrics_table_png(out_path: Path, metrics_dict: dict):
    rows = []

    ordered_keys = [
        "model",
        "threshold",
        "rows_evaluated",
        "accuracy",
        "precision",
        "recall",
        "specificity",
        "f1",
        "roc_auc",
        "pr_auc",
        "mcc",
        "brier",
        "best_f1_threshold_from_sweep",
        "best_f1_from_sweep",
    ]

    for key in ordered_keys:
        if key in metrics_dict:
            value = metrics_dict[key]
            if isinstance(value, float):
                display_value = f"{value:.4f}"
            else:
                display_value = str(value)
            rows.append([key, display_value])

    fig_height = max(3, 0.45 * len(rows) + 1)
    fig, ax = plt.subplots(figsize=(9, fig_height))
    ax.axis("off")

    table = ax.table(
        cellText=rows,
        colLabels=["Metric", "Value"],
        loc="center",
        cellLoc="left",
        colLoc="left"
    )
    table.auto_set_font_size(False)
    table.set_fontsize(10)
    table.scale(1, 1.2)

    plt.tight_layout()
    plt.savefig(out_path, dpi=250, bbox_inches="tight")
    plt.close(fig)


def _metrics_at_threshold(y_true: np.ndarray, probs: np.ndarray, thr: float) -> dict:
    y_pred = (probs >= thr).astype(int)

    acc = float(accuracy_score(y_true, y_pred))
    prec = float(precision_score(y_true, y_pred, zero_division=0))
    rec = float(recall_score(y_true, y_pred, zero_division=0))
    f1 = float(f1_score(y_true, y_pred, zero_division=0))

    cm = confusion_matrix(y_true, y_pred, labels=[0, 1])
    tn, fp, fn, tp = cm.ravel()

    specificity = float(tn / (tn + fp)) if (tn + fp) > 0 else 0.0

    try:
        mcc = float(matthews_corrcoef(y_true, y_pred))
    except Exception:
        mcc = 0.0

    try:
        auc = float(roc_auc_score(y_true, probs))
    except Exception:
        auc = None

    try:
        pr_auc = float(average_precision_score(y_true, probs))
    except Exception:
        pr_auc = None

    try:
        brier = float(brier_score_loss(y_true, probs))
    except Exception:
        brier = None

    return {
        "threshold": float(thr),
        "accuracy": acc,
        "precision": prec,
        "recall": rec,
        "specificity": specificity,
        "f1": f1,
        "roc_auc": auc,
        "pr_auc": pr_auc,
        "mcc": mcc,
        "brier": brier,
        "confusion_matrix": {
            "tn": int(tn),
            "fp": int(fp),
            "fn": int(fn),
            "tp": int(tp),
        },
    }


def _threshold_sweep(y_true: np.ndarray, probs: np.ndarray, out_csv: Path):
    rows = []

    for thr in np.linspace(0.05, 0.95, 19):
        m = _metrics_at_threshold(y_true, probs, float(thr))
        rows.append({
            "threshold": m["threshold"],
            "accuracy": m["accuracy"],
            "precision": m["precision"],
            "recall": m["recall"],
            "specificity": m["specificity"],
            "f1": m["f1"],
            "mcc": m["mcc"],
        })

    df = pd.DataFrame(rows)
    df.to_csv(out_csv, index=False)

    best_row = df.loc[df["f1"].idxmax()].to_dict()
    return best_row, df


def main():
    # src/evaluate_model.py
    src_dir = Path(__file__).resolve().parent
    root_dir = src_dir.parent

    data_path = root_dir / "data" / "raw" / "student_dropout.csv"
    figures_dir = root_dir / "figures"
    metrics_dir = root_dir / "metrics"

    figures_dir.mkdir(parents=True, exist_ok=True)
    metrics_dir.mkdir(parents=True, exist_ok=True)

    if not data_path.exists():
        raise FileNotFoundError(f"Dataset not found: {data_path}")

    artifacts = get_artifacts()
    model = artifacts["model"]
    metadata = artifacts["metadata"]
    training_medians = artifacts["training_medians"]

    feature_columns = metadata["feature_columns"]
    threshold = float(metadata.get("threshold", 0.50))
    model_name = metadata.get("model_name", "XGBoost")

    df = pd.read_csv(data_path)
    df.columns = [c.strip() for c in df.columns]

    if "Target" not in df.columns:
        raise ValueError("Dataset must contain 'Target' column")

    df["Target"] = df["Target"].astype(str).str.strip()
    df = df[df["Target"].isin(["Dropout", "Graduate", "Enrolled"])].copy()

    # Binary label: Dropout = 1, others = 0
    y_true = (df["Target"] == "Dropout").astype(int).values

    # Remove label before preprocessing
    records = df.drop(columns=["Target"], errors="ignore").to_dict(orient="records")

    X = prepare_dataframe(
        records=records,
        feature_columns=feature_columns,
        training_medians=training_medians
    )

    probs = model.predict_proba(X)[:, 1]
    y_pred = (probs >= threshold).astype(int)

    # Main metrics at deployed threshold
    m = _metrics_at_threshold(y_true, probs, threshold)

    metrics = {
        "model": model_name,
        "threshold": threshold,
        "rows_evaluated": int(len(df)),
        "accuracy": m["accuracy"],
        "precision": m["precision"],
        "recall": m["recall"],
        "specificity": m["specificity"],
        "f1": m["f1"],
        "roc_auc": m["roc_auc"],
        "pr_auc": m["pr_auc"],
        "mcc": m["mcc"],
        "brier": m["brier"],
        "confusion_matrix": m["confusion_matrix"],
        "generated_at": datetime.utcnow().isoformat() + "Z"
    }

    # Threshold sweep
    best, _ = _threshold_sweep(y_true, probs, metrics_dir / "dropout_threshold_sweep.csv")
    metrics["best_f1_threshold_from_sweep"] = float(best["threshold"])
    metrics["best_f1_from_sweep"] = float(best["f1"])

    # Save JSON metrics
    with open(metrics_dir / "dropout_metrics.json", "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2)

    # Save classification report
    report_txt = classification_report(
        y_true,
        y_pred,
        target_names=["Not Dropout", "Dropout"],
        zero_division=0
    )
    (metrics_dir / "dropout_classification_report.txt").write_text(report_txt, encoding="utf-8")

    # Save CSV metrics table
    metrics_table = pd.DataFrame([{
        "model": model_name,
        "threshold": threshold,
        "rows": len(df),
        "accuracy": metrics["accuracy"],
        "precision": metrics["precision"],
        "recall": metrics["recall"],
        "specificity": metrics["specificity"],
        "f1": metrics["f1"],
        "roc_auc": metrics["roc_auc"],
        "pr_auc": metrics["pr_auc"],
        "mcc": metrics["mcc"],
        "brier": metrics["brier"],
        "best_f1_threshold_sweep": metrics["best_f1_threshold_from_sweep"],
        "best_f1_sweep": metrics["best_f1_from_sweep"],
    }])
    metrics_table.to_csv(metrics_dir / "dropout_metrics_table.csv", index=False)

    # Save PNG metrics table
    _save_metrics_table_png(figures_dir / "dropout_metrics_table.png", metrics)

    # Confusion Matrix (counts)
    cm = confusion_matrix(y_true, y_pred, labels=[0, 1])

    fig = plt.figure()
    ax = fig.add_subplot(111)
    disp = ConfusionMatrixDisplay(
        confusion_matrix=cm,
        display_labels=["Not Dropout", "Dropout"]
    )
    disp.plot(ax=ax, values_format="d")
    ax.set_title(f"Confusion Matrix - {model_name} (thr={threshold:.2f})")
    plt.tight_layout()
    plt.savefig(figures_dir / "dropout_confusion_matrix.png", dpi=220)
    plt.close(fig)

    # Confusion Matrix (normalized) - sklearn version safe
    cm_norm = cm.astype(float)
    row_sums = cm_norm.sum(axis=1, keepdims=True)
    cm_norm = np.divide(
        cm_norm,
        row_sums,
        out=np.zeros_like(cm_norm),
        where=row_sums != 0
    )

    fig = plt.figure()
    ax = fig.add_subplot(111)
    disp = ConfusionMatrixDisplay(
        confusion_matrix=cm_norm,
        display_labels=["Not Dropout", "Dropout"]
    )
    disp.plot(ax=ax, values_format=".2f")
    ax.set_title(f"Normalized Confusion Matrix - {model_name} (thr={threshold:.2f})")
    plt.tight_layout()
    plt.savefig(figures_dir / "dropout_confusion_matrix_normalized.png", dpi=220)
    plt.close(fig)

    # ROC Curve
    try:
        fig = plt.figure()
        ax = fig.add_subplot(111)
        RocCurveDisplay.from_predictions(y_true, probs, ax=ax)
        ax.set_title(f"ROC Curve - {model_name}")
        plt.tight_layout()
        plt.savefig(figures_dir / "dropout_roc_curve.png", dpi=220)
        plt.close(fig)
    except Exception as e:
        print(f"⚠️ ROC curve could not be generated: {e}")

    # Precision-Recall Curve
    try:
        fig = plt.figure()
        ax = fig.add_subplot(111)
        PrecisionRecallDisplay.from_predictions(y_true, probs, ax=ax)
        ax.set_title(f"Precision-Recall Curve - {model_name}")
        plt.tight_layout()
        plt.savefig(figures_dir / "dropout_pr_curve.png", dpi=220)
        plt.close(fig)
    except Exception as e:
        print(f"⚠️ PR curve could not be generated: {e}")

    print("✅ Dropout evidence pack generated successfully")
    print(f"Metrics folder: {metrics_dir}")
    print(f"Figures folder: {figures_dir}")


if __name__ == "__main__":
    main()