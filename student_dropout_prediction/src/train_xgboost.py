from pathlib import Path
import json
import warnings

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from xgboost import XGBClassifier
from sklearn.model_selection import train_test_split, StratifiedKFold, cross_val_score
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    classification_report,
    roc_curve,
    precision_recall_curve,
    average_precision_score
)
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

warnings.filterwarnings("ignore")


# =========================
# PATH SETUP
# =========================
ROOT_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = ROOT_DIR / "data" / "raw" / "student_dropout.csv"
MODEL_DIR = ROOT_DIR / "models"
FIGURE_DIR = ROOT_DIR / "reports" / "figures"
METRIC_DIR = ROOT_DIR / "reports" / "metrics"

MODEL_DIR.mkdir(parents=True, exist_ok=True)
FIGURE_DIR.mkdir(parents=True, exist_ok=True)
METRIC_DIR.mkdir(parents=True, exist_ok=True)


# =========================
# LOAD DATA
# =========================
print("Loading dataset...")
df = pd.read_csv(DATA_PATH)

df.columns = [col.strip() for col in df.columns]
df["Target"] = df["Target"].astype(str).str.strip()

print("Dataset shape:", df.shape)
print("Target distribution:")
print(df["Target"].value_counts())
print()


# =========================
# DATA QUALITY REPORT
# =========================
duplicate_count = int(df.duplicated().sum())
missing_by_column = df.isnull().sum().to_dict()

data_quality_report = {
    "total_rows": int(df.shape[0]),
    "total_columns": int(df.shape[1]),
    "duplicate_rows": duplicate_count,
    "missing_values_by_column": {k: int(v) for k, v in missing_by_column.items()}
}

with open(METRIC_DIR / "data_quality_report.json", "w") as f:
    json.dump(data_quality_report, f, indent=4)

print(f"Duplicate rows: {duplicate_count}")
print("Data quality report saved.")
print()


# =========================
# CREATE BINARY TARGET
# 1 = Dropout
# 0 = Not Dropout
# =========================
df = df[df["Target"].isin(["Dropout", "Graduate", "Enrolled"])].copy()
df["dropout_label"] = (df["Target"] == "Dropout").astype(int)

print("Binary target distribution:")
print(df["dropout_label"].value_counts())
print()


# =========================
# FEATURE / TARGET SPLIT
# =========================
X = df.drop(columns=["Target", "dropout_label"]).copy()
y = df["dropout_label"].copy()

feature_columns = list(X.columns)

# Convert all features to numeric
for col in X.columns:
    X[col] = pd.to_numeric(X[col], errors="coerce")
    X[col] = X[col].fillna(X[col].median())


# =========================
# TRAIN / VALIDATION / TEST SPLIT
# =========================
X_train_full, X_test, y_train_full, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

X_train, X_val, y_train, y_val = train_test_split(
    X_train_full,
    y_train_full,
    test_size=0.20,
    random_state=42,
    stratify=y_train_full
)

print("Train shape:", X_train.shape)
print("Validation shape:", X_val.shape)
print("Test shape:", X_test.shape)
print()


# =========================
# HANDLE CLASS IMBALANCE
# =========================
neg_count = int((y_train == 0).sum())
pos_count = int((y_train == 1).sum())
scale_pos_weight = neg_count / pos_count

print(f"Negative class count: {neg_count}")
print(f"Positive class count: {pos_count}")
print(f"scale_pos_weight: {scale_pos_weight:.4f}")
print()


# =========================
# BASELINE MODEL COMPARISON
# =========================
print("Training baseline models for comparison...")

baseline_models = {
    "LogisticRegression": Pipeline([
        ("scaler", StandardScaler()),
        ("model", LogisticRegression(
            max_iter=2000,
            class_weight="balanced",
            random_state=42
        ))
    ]),
    "RandomForest": RandomForestClassifier(
        n_estimators=400,
        max_depth=10,
        min_samples_split=6,
        min_samples_leaf=3,
        class_weight="balanced",
        random_state=42,
        n_jobs=-1
    )
}

comparison_rows = []

for model_name, baseline_model in baseline_models.items():
    baseline_model.fit(X_train, y_train)
    val_prob_baseline = baseline_model.predict_proba(X_val)[:, 1]
    val_pred_baseline = (val_prob_baseline >= 0.5).astype(int)

    comparison_rows.append({
        "model": model_name,
        "validation_accuracy": float(accuracy_score(y_val, val_pred_baseline)),
        "validation_precision": float(precision_score(y_val, val_pred_baseline, zero_division=0)),
        "validation_recall": float(recall_score(y_val, val_pred_baseline, zero_division=0)),
        "validation_f1": float(f1_score(y_val, val_pred_baseline, zero_division=0)),
        "validation_roc_auc": float(roc_auc_score(y_val, val_prob_baseline))
    })

comparison_df = pd.DataFrame(comparison_rows)
comparison_df.to_csv(METRIC_DIR / "baseline_model_comparison.csv", index=False)

print("Baseline comparison saved.")
print(comparison_df)
print()


# =========================
# TRAIN XGBOOST MODEL
# =========================
print("Training XGBoost model...")

model = XGBClassifier(
    objective="binary:logistic",
    eval_metric="auc",
    n_estimators=3000,
    learning_rate=0.03,
    max_depth=5,
    min_child_weight=3,
    subsample=0.85,
    colsample_bytree=0.85,
    gamma=0.1,
    reg_alpha=0.1,
    reg_lambda=2.0,
    scale_pos_weight=scale_pos_weight,
    random_state=42,
    n_jobs=-1,
    tree_method="hist",
    early_stopping_rounds=100
)

model.fit(
    X_train,
    y_train,
    eval_set=[(X_val, y_val)],
    verbose=100
)


# =========================
# THRESHOLD TUNING ON VALIDATION SET
# =========================
print("\nSearching best threshold on validation set...")

val_prob = model.predict_proba(X_val)[:, 1]

threshold_rows = []
best_threshold = 0.50
best_f1 = -1
best_recall = -1
best_precision = -1

for threshold in np.arange(0.30, 0.71, 0.01):
    val_pred = (val_prob >= threshold).astype(int)

    precision = precision_score(y_val, val_pred, zero_division=0)
    recall = recall_score(y_val, val_pred, zero_division=0)
    f1 = f1_score(y_val, val_pred, zero_division=0)
    accuracy = accuracy_score(y_val, val_pred)

    threshold_rows.append({
        "threshold": round(float(threshold), 2),
        "accuracy": float(accuracy),
        "precision": float(precision),
        "recall": float(recall),
        "f1_score": float(f1)
    })

    if (f1 > best_f1) or (np.isclose(f1, best_f1) and recall > best_recall):
        best_f1 = f1
        best_recall = recall
        best_precision = precision
        best_threshold = float(threshold)

threshold_df = pd.DataFrame(threshold_rows)
threshold_df.to_csv(METRIC_DIR / "threshold_search_results_xgboost.csv", index=False)

print(f"Best threshold found: {best_threshold:.2f}")
print(f"Validation Precision : {best_precision:.4f}")
print(f"Validation Recall    : {best_recall:.4f}")
print(f"Validation F1        : {best_f1:.4f}")
print()


# =========================
# CROSS-VALIDATION EVIDENCE
# =========================
print("Running 5-fold cross-validation for stronger evidence...")

cv_model = XGBClassifier(
    objective="binary:logistic",
    eval_metric="auc",
    n_estimators=500,
    learning_rate=0.03,
    max_depth=5,
    min_child_weight=3,
    subsample=0.85,
    colsample_bytree=0.85,
    gamma=0.1,
    reg_alpha=0.1,
    reg_lambda=2.0,
    scale_pos_weight=(y_train_full == 0).sum() / (y_train_full == 1).sum(),
    random_state=42,
    n_jobs=-1,
    tree_method="hist"
)

cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
cv_auc_scores = cross_val_score(
    cv_model,
    X_train_full,
    y_train_full,
    cv=cv,
    scoring="roc_auc",
    n_jobs=-1
)

cv_report = {
    "cv_roc_auc_scores": [float(score) for score in cv_auc_scores],
    "cv_roc_auc_mean": float(np.mean(cv_auc_scores)),
    "cv_roc_auc_std": float(np.std(cv_auc_scores))
}

with open(METRIC_DIR / "cross_validation_report_xgboost.json", "w") as f:
    json.dump(cv_report, f, indent=4)

print(f"CV ROC-AUC Mean: {np.mean(cv_auc_scores):.4f}")
print(f"CV ROC-AUC Std : {np.std(cv_auc_scores):.4f}")
print()


# =========================
# TEST EVALUATION
# =========================
y_prob = model.predict_proba(X_test)[:, 1]
y_pred = (y_prob >= best_threshold).astype(int)

accuracy = accuracy_score(y_test, y_pred)
precision = precision_score(y_test, y_pred, zero_division=0)
recall = recall_score(y_test, y_pred, zero_division=0)
f1 = f1_score(y_test, y_pred, zero_division=0)
roc_auc = roc_auc_score(y_test, y_prob)
avg_precision = average_precision_score(y_test, y_prob)

print("\n===== TEST METRICS =====")
print(f"Threshold: {best_threshold:.2f}")
print(f"Accuracy : {accuracy:.4f}")
print(f"Precision: {precision:.4f}")
print(f"Recall   : {recall:.4f}")
print(f"F1 Score : {f1:.4f}")
print(f"ROC AUC  : {roc_auc:.4f}")
print(f"PR AUC   : {avg_precision:.4f}")

report_text = classification_report(
    y_test,
    y_pred,
    target_names=["Not Dropout", "Dropout"]
)

print("\nClassification Report:")
print(report_text)


# =========================
# SAVE METRICS
# =========================
metrics = {
    "model_name": "XGBoost",
    "threshold": float(best_threshold),
    "accuracy": float(accuracy),
    "precision": float(precision),
    "recall": float(recall),
    "f1_score": float(f1),
    "roc_auc": float(roc_auc),
    "average_precision": float(avg_precision),
    "train_rows": int(len(X_train)),
    "validation_rows": int(len(X_val)),
    "test_rows": int(len(X_test)),
    "negative_class_count_train": int(neg_count),
    "positive_class_count_train": int(pos_count),
    "scale_pos_weight": float(scale_pos_weight),
    "cv_roc_auc_mean": float(np.mean(cv_auc_scores)),
    "cv_roc_auc_std": float(np.std(cv_auc_scores))
}

with open(METRIC_DIR / "dropout_xgboost_metrics.json", "w") as f:
    json.dump(metrics, f, indent=4)

with open(METRIC_DIR / "classification_report_xgboost.txt", "w", encoding="utf-8") as f:
    f.write(report_text)


# =========================
# CONFUSION MATRIX
# =========================
cm = confusion_matrix(y_test, y_pred)

plt.figure(figsize=(6, 5))
plt.imshow(cm, interpolation="nearest")
plt.title("Confusion Matrix - XGBoost Dropout Prediction")
plt.colorbar()

tick_marks = np.arange(2)
plt.xticks(tick_marks, ["Not Dropout", "Dropout"])
plt.yticks(tick_marks, ["Not Dropout", "Dropout"])

for i in range(cm.shape[0]):
    for j in range(cm.shape[1]):
        plt.text(j, i, str(cm[i, j]), ha="center", va="center")

plt.ylabel("Actual")
plt.xlabel("Predicted")
plt.tight_layout()
plt.savefig(FIGURE_DIR / "confusion_matrix_xgboost.png", dpi=300)
plt.close()


# =========================
# ROC CURVE
# =========================
fpr, tpr, _ = roc_curve(y_test, y_prob)

plt.figure(figsize=(6, 5))
plt.plot(fpr, tpr, label=f"ROC AUC = {roc_auc:.4f}")
plt.plot([0, 1], [0, 1], linestyle="--")
plt.xlabel("False Positive Rate")
plt.ylabel("True Positive Rate")
plt.title("ROC Curve - XGBoost Dropout Prediction")
plt.legend()
plt.tight_layout()
plt.savefig(FIGURE_DIR / "roc_curve_xgboost.png", dpi=300)
plt.close()


# =========================
# PRECISION-RECALL CURVE
# =========================
precision_curve, recall_curve, _ = precision_recall_curve(y_test, y_prob)

plt.figure(figsize=(6, 5))
plt.plot(recall_curve, precision_curve, label=f"PR AUC = {avg_precision:.4f}")
plt.xlabel("Recall")
plt.ylabel("Precision")
plt.title("Precision-Recall Curve - XGBoost Dropout Prediction")
plt.legend()
plt.tight_layout()
plt.savefig(FIGURE_DIR / "precision_recall_curve_xgboost.png", dpi=300)
plt.close()


# =========================
# FEATURE IMPORTANCE
# =========================
feature_importance = pd.DataFrame({
    "feature": X.columns,
    "importance": model.feature_importances_
}).sort_values(by="importance", ascending=False)

feature_importance.to_csv(METRIC_DIR / "feature_importance_xgboost.csv", index=False)

top_n = 15
top_features = feature_importance.head(top_n)

plt.figure(figsize=(10, 6))
plt.barh(top_features["feature"][::-1], top_features["importance"][::-1])
plt.xlabel("Importance")
plt.title("Top Feature Importances - XGBoost Dropout Prediction")
plt.tight_layout()
plt.savefig(FIGURE_DIR / "feature_importance_top15_xgboost.png", dpi=300)
plt.close()


# =========================
# SAVE MODEL
# =========================
model.save_model(str(MODEL_DIR / "dropout_xgboost_model.json"))

metadata = {
    "model_name": "XGBoost",
    "target_type": "binary",
    "positive_class": "Dropout",
    "negative_class": "Graduate_or_Enrolled",
    "feature_columns": feature_columns,
    "threshold": float(best_threshold)
}

with open(MODEL_DIR / "dropout_xgboost_metadata.json", "w") as f:
    json.dump(metadata, f, indent=4)

print("\nModel saved to:", MODEL_DIR / "dropout_xgboost_model.json")
print("Metrics saved to:", METRIC_DIR)
print("Figures saved to:", FIGURE_DIR)
print("\nTraining complete.")