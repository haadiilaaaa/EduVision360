from pathlib import Path
import json
import pandas as pd
import xgboost as xgb

# =========================
# PATH SETUP
# =========================
ROOT_DIR = Path(__file__).resolve().parent.parent

TRAIN_DATA_PATH = ROOT_DIR / "data" / "raw" / "student_dropout.csv"
INPUT_DATA_PATH = ROOT_DIR / "data" / "input" / "new_student.csv"

MODEL_PATH = ROOT_DIR / "models" / "dropout_xgboost_model.json"
METRICS_PATH = ROOT_DIR / "reports" / "metrics" / "dropout_xgboost_metrics.json"
METADATA_PATH = ROOT_DIR / "models" / "dropout_xgboost_metadata.json"

OUTPUT_PATH = ROOT_DIR / "reports" / "predictions" / "dropout_predictions.csv"

OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)


# =========================
# LOAD THRESHOLD
# =========================
def load_threshold():
    if METRICS_PATH.exists():
        try:
            with open(METRICS_PATH, "r", encoding="utf-8") as f:
                metrics = json.load(f)
            return float(metrics.get("threshold", 0.54))
        except Exception:
            return 0.54
    return 0.54


# =========================
# LOAD FEATURE COLUMNS
# =========================
def load_feature_columns():
    if not METADATA_PATH.exists():
        raise FileNotFoundError(f"Metadata file not found: {METADATA_PATH}")

    with open(METADATA_PATH, "r", encoding="utf-8") as f:
        metadata = json.load(f)

    feature_columns = metadata.get("feature_columns")
    if not feature_columns:
        raise ValueError("feature_columns not found in metadata file")

    return feature_columns


# =========================
# GET TRAINING MEDIANS
# same preprocessing logic as training
# =========================
def get_training_medians(feature_columns):
    if not TRAIN_DATA_PATH.exists():
        raise FileNotFoundError(f"Training dataset not found: {TRAIN_DATA_PATH}")

    df_train = pd.read_csv(TRAIN_DATA_PATH)
    df_train.columns = [col.strip() for col in df_train.columns]

    if "Target" in df_train.columns:
        df_train["Target"] = df_train["Target"].astype(str).str.strip()
        df_train = df_train[df_train["Target"].isin(["Dropout", "Graduate", "Enrolled"])].copy()
        df_train = df_train.drop(columns=["Target"])

    if "dropout_label" in df_train.columns:
        df_train = df_train.drop(columns=["dropout_label"])

    medians = {}

    for col in feature_columns:
        if col in df_train.columns:
            df_train[col] = pd.to_numeric(df_train[col], errors="coerce")
            medians[col] = df_train[col].median()
        else:
            medians[col] = 0

    return medians


# =========================
# RISK LEVEL
# =========================
def get_risk_level(prob):
    if prob >= 0.70:
        return "HIGH"
    elif prob >= 0.40:
        return "MEDIUM"
    else:
        return "LOW"


# =========================
# PREPARE INPUT DATA
# =========================
def prepare_input_data(feature_columns, training_medians):
    if not INPUT_DATA_PATH.exists():
        raise FileNotFoundError(
            f"Input file not found: {INPUT_DATA_PATH}\n"
            f"Create new_student.csv first."
        )

    df_input = pd.read_csv(INPUT_DATA_PATH)
    df_input.columns = [col.strip() for col in df_input.columns]

    # Remove target columns if user accidentally included them
    for col in ["Target", "dropout_label"]:
        if col in df_input.columns:
            df_input = df_input.drop(columns=[col])

    df_original = df_input.copy()

    # Add missing columns
    for col in feature_columns:
        if col not in df_input.columns:
            df_input[col] = training_medians.get(col, 0)

    # Keep only training feature columns in exact order
    df_input = df_input[feature_columns].copy()

    # Convert to numeric and fill missing values using training medians
    for col in feature_columns:
        df_input[col] = pd.to_numeric(df_input[col], errors="coerce")
        df_input[col] = df_input[col].fillna(training_medians.get(col, 0))

    return df_original, df_input


# =========================
# MAIN
# =========================
def main():
    print("Loading metadata...")
    feature_columns = load_feature_columns()

    print("Loading training medians...")
    training_medians = get_training_medians(feature_columns)

    print("Loading new input data...")
    df_original, X_input = prepare_input_data(feature_columns, training_medians)

    print("Loading trained XGBoost model...")
    model = xgb.XGBClassifier()
    model.load_model(MODEL_PATH)

    threshold = load_threshold()
    print(f"Using decision threshold: {threshold:.2f}")

    probabilities = model.predict_proba(X_input)[:, 1]
    predictions = (probabilities >= threshold).astype(int)

    label_map = {
        0: "Not Dropout",
        1: "Dropout"
    }

    result_df = df_original.copy()
    result_df["dropout_probability"] = probabilities.round(4)
    result_df["predicted_label"] = [label_map[p] for p in predictions]
    result_df["risk_level"] = [get_risk_level(p) for p in probabilities]

    result_df.to_csv(OUTPUT_PATH, index=False)

    print("\n===== PREDICTION RESULTS =====")
    for i, row in result_df.iterrows():
        print(f"\nStudent {i + 1}")
        print(f"Predicted Label     : {row['predicted_label']}")
        print(f"Dropout Probability : {row['dropout_probability']}")
        print(f"Risk Level          : {row['risk_level']}")

    print(f"\nPredictions saved to: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()