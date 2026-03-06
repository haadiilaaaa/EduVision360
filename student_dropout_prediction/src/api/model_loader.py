from functools import lru_cache
from pathlib import Path
import json

import pandas as pd
import xgboost as xgb


ROOT_DIR = Path(__file__).resolve().parents[2]

MODEL_PATH = ROOT_DIR / "models" / "dropout_xgboost_model.json"
METADATA_PATH = ROOT_DIR / "models" / "dropout_xgboost_metadata.json"
TRAINING_MEDIANS_PATH = ROOT_DIR / "models" / "training_medians.json"
TRAIN_DATA_PATH = ROOT_DIR / "data" / "raw" / "student_dropout.csv"


def _safe_float(value, default=0.0):
    try:
        return float(value)
    except (TypeError, ValueError):
        return float(default)


@lru_cache(maxsize=1)
def load_metadata():
    if not METADATA_PATH.exists():
        raise FileNotFoundError(f"Metadata file not found: {METADATA_PATH}")

    with open(METADATA_PATH, "r", encoding="utf-8") as f:
        metadata = json.load(f)

    feature_columns = metadata.get("feature_columns")
    if not feature_columns:
        raise ValueError("feature_columns not found in metadata file")

    metadata["threshold"] = _safe_float(metadata.get("threshold", 0.50), 0.50)
    metadata["model_name"] = metadata.get("model_name", "XGBoost")

    return metadata


def _compute_training_medians(feature_columns):
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
            median_value = df_train[col].median()
            medians[col] = 0.0 if pd.isna(median_value) else float(median_value)
        else:
            medians[col] = 0.0

    return medians


@lru_cache(maxsize=1)
def load_training_medians():
    metadata = load_metadata()
    feature_columns = metadata["feature_columns"]

    if TRAINING_MEDIANS_PATH.exists():
        with open(TRAINING_MEDIANS_PATH, "r", encoding="utf-8") as f:
            saved_medians = json.load(f)

        return {
            col: _safe_float(saved_medians.get(col, 0.0), 0.0)
            for col in feature_columns
        }

    return _compute_training_medians(feature_columns)


@lru_cache(maxsize=1)
def load_model():
    if not MODEL_PATH.exists():
        raise FileNotFoundError(f"Model file not found: {MODEL_PATH}")

    model = xgb.XGBClassifier()
    model.load_model(str(MODEL_PATH))
    return model


@lru_cache(maxsize=1)
def get_artifacts():
    metadata = load_metadata()
    training_medians = load_training_medians()
    model = load_model()

    return {
        "metadata": metadata,
        "training_medians": training_medians,
        "model": model
    }