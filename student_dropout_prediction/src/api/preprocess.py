from typing import Any, Dict, List
import pandas as pd


def _clean_feature_keys(features: Dict[str, Any]) -> Dict[str, Any]:
    cleaned = {}
    for key, value in features.items():
        cleaned[str(key).strip()] = value
    return cleaned


def _to_numeric(value: Any, default: float) -> float:
    if value is None:
        return float(default)

    if isinstance(value, str):
        value = value.strip()
        if value == "":
            return float(default)

    try:
        return float(value)
    except (TypeError, ValueError):
        return float(default)


def prepare_single_record(
    raw_features: Dict[str, Any],
    feature_columns: List[str],
    training_medians: Dict[str, float]
) -> Dict[str, float]:
    cleaned_features = _clean_feature_keys(raw_features)
    row = {}

    for col in feature_columns:
        default_value = training_medians.get(col, 0.0)
        raw_value = cleaned_features.get(col, default_value)
        row[col] = _to_numeric(raw_value, default_value)

    return row


def prepare_dataframe(
    records: List[Dict[str, Any]],
    feature_columns: List[str],
    training_medians: Dict[str, float]
) -> pd.DataFrame:
    rows = [
        prepare_single_record(record, feature_columns, training_medians)
        for record in records
    ]
    return pd.DataFrame(rows, columns=feature_columns)