from pathlib import Path
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from catboost import CatBoostClassifier, Pool
from sklearn.model_selection import train_test_split
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    classification_report,
    roc_curve
)
from sklearn.utils.class_weight import compute_class_weight


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

# Clean column names
df.columns = [col.strip() for col in df.columns]

# Clean target values
df["Target"] = df["Target"].astype(str).str.strip()

print("Dataset shape:", df.shape)
print("Target distribution:")
print(df["Target"].value_counts())
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
X = df.drop(columns=["Target", "dropout_label"])
y = df["dropout_label"]


# =========================
# DEFINE CATEGORICAL COLUMNS
# These are coded categories in the dataset
# =========================
categorical_cols = [
    "Marital status",
    "Application mode",
    "Application order",
    "Course",
    "Daytime/evening attendance",
    "Previous qualification",
    "Nacionality",
    "Mother's qualification",
    "Father's qualification",
    "Mother's occupation",
    "Father's occupation",
    "Displaced",
    "Educational special needs",
    "Debtor",
    "Tuition fees up to date",
    "Gender",
    "Scholarship holder",
    "International"
]

numeric_cols = [col for col in X.columns if col not in categorical_cols]

# Convert categorical columns to string so CatBoost treats them as categorical
for col in categorical_cols:
    X[col] = X[col].astype(str)

# Convert numeric columns properly
for col in numeric_cols:
    X[col] = pd.to_numeric(X[col], errors="coerce")

# Fill any missing values just in case
for col in categorical_cols:
    X[col] = X[col].fillna("Unknown")

for col in numeric_cols:
    X[col] = X[col].fillna(X[col].median())


# =========================
# TRAIN / VALIDATION / TEST SPLIT
# =========================
X_train_full, X_test, y_train_full, y_test = train_test_split(
    X, y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

X_train, X_val, y_train, y_val = train_test_split(
    X_train_full, y_train_full,
    test_size=0.20,
    random_state=42,
    stratify=y_train_full
)

print("Train shape:", X_train.shape)
print("Validation shape:", X_val.shape)
print("Test shape:", X_test.shape)
print()


# =========================
# CLASS WEIGHTS
# Helps if classes are imbalanced
# =========================
classes = np.array([0, 1])
weights = compute_class_weight(class_weight="balanced", classes=classes, y=y_train)
class_weights = [weights[0], weights[1]]

print("Class weights:", class_weights)
print()


# =========================
# CATBOOST POOLS
# =========================
cat_feature_indices = [X.columns.get_loc(col) for col in categorical_cols]

train_pool = Pool(X_train, y_train, cat_features=cat_feature_indices)
val_pool = Pool(X_val, y_val, cat_features=cat_feature_indices)
test_pool = Pool(X_test, y_test, cat_features=cat_feature_indices)


# =========================
# TRAIN MODEL
# =========================
print("Training CatBoost model...")

model = CatBoostClassifier(
    iterations=1500,
    learning_rate=0.03,
    depth=6,
    loss_function="Logloss",
    eval_metric="F1",
    random_seed=42,
    class_weights=class_weights,
    verbose=100
)

model.fit(
    train_pool,
    eval_set=val_pool,
    use_best_model=True,
    early_stopping_rounds=100
)


# =========================
# EVALUATION
# =========================
y_prob = model.predict_proba(test_pool)[:, 1]
y_pred = (y_prob >= 0.5).astype(int)

accuracy = accuracy_score(y_test, y_pred)
precision = precision_score(y_test, y_pred, zero_division=0)
recall = recall_score(y_test, y_pred, zero_division=0)
f1 = f1_score(y_test, y_pred, zero_division=0)
roc_auc = roc_auc_score(y_test, y_prob)

print("\n===== TEST METRICS =====")
print(f"Accuracy : {accuracy:.4f}")
print(f"Precision: {precision:.4f}")
print(f"Recall   : {recall:.4f}")
print(f"F1 Score : {f1:.4f}")
print(f"ROC AUC  : {roc_auc:.4f}")

report_text = classification_report(y_test, y_pred, target_names=["Not Dropout", "Dropout"])
print("\nClassification Report:")
print(report_text)


# =========================
# SAVE METRICS
# =========================
metrics = {
    "accuracy": float(accuracy),
    "precision": float(precision),
    "recall": float(recall),
    "f1_score": float(f1),
    "roc_auc": float(roc_auc),
    "train_rows": int(len(X_train)),
    "validation_rows": int(len(X_val)),
    "test_rows": int(len(X_test))
}

with open(METRIC_DIR / "dropout_model_metrics.json", "w") as f:
    json.dump(metrics, f, indent=4)

with open(METRIC_DIR / "classification_report.txt", "w", encoding="utf-8") as f:
    f.write(report_text)


# =========================
# CONFUSION MATRIX
# =========================
cm = confusion_matrix(y_test, y_pred)

plt.figure(figsize=(6, 5))
plt.imshow(cm, interpolation="nearest")
plt.title("Confusion Matrix - Dropout Prediction")
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
plt.savefig(FIGURE_DIR / "confusion_matrix.png", dpi=300)
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
plt.title("ROC Curve - Dropout Prediction")
plt.legend()
plt.tight_layout()
plt.savefig(FIGURE_DIR / "roc_curve.png", dpi=300)
plt.close()


# =========================
# FEATURE IMPORTANCE
# =========================
feature_importance = pd.DataFrame({
    "feature": X.columns,
    "importance": model.get_feature_importance()
}).sort_values(by="importance", ascending=False)

feature_importance.to_csv(METRIC_DIR / "feature_importance.csv", index=False)

top_n = 15
top_features = feature_importance.head(top_n)

plt.figure(figsize=(10, 6))
plt.barh(top_features["feature"][::-1], top_features["importance"][::-1])
plt.xlabel("Importance")
plt.title("Top Feature Importances - Dropout Prediction")
plt.tight_layout()
plt.savefig(FIGURE_DIR / "feature_importance_top15.png", dpi=300)
plt.close()


# =========================
# SAVE MODEL
# =========================
model.save_model(str(MODEL_DIR / "dropout_catboost_model.cbm"))

metadata = {
    "target_type": "binary",
    "positive_class": "Dropout",
    "negative_class": "Graduate_or_Enrolled",
    "categorical_columns": categorical_cols,
    "numeric_columns": numeric_cols,
    "threshold": 0.5
}

with open(MODEL_DIR / "dropout_model_metadata.json", "w") as f:
    json.dump(metadata, f, indent=4)

print("\nModel saved to:", MODEL_DIR / "dropout_catboost_model.cbm")
print("Metrics saved to:", METRIC_DIR)
print("Figures saved to:", FIGURE_DIR)
print("\nTraining complete.")