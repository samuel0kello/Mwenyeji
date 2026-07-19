import json
from pathlib import Path

import numpy as np
from tensorflow import keras

from .data_loader import build_contextual_guide


def load_artifacts(model_path, stop_mapping_path, guide_suggestions_path):
    model = keras.models.load_model(model_path)

    with Path(stop_mapping_path).open('r', encoding='utf-8') as handle:
        stop_mapping = json.load(handle).get('stop_id_to_index', {})

    with Path(guide_suggestions_path).open('r', encoding='utf-8') as handle:
        guide_catalog = json.load(handle)

    guide_lookup = {entry.get('index', idx): entry for idx, entry in enumerate(guide_catalog)}
    return model, stop_mapping, guide_lookup


def build_feature_vector(stop_id, arrival_time, stop_sequence, stop_mapping):
    if stop_id not in stop_mapping:
        raise KeyError(f'Stop ID {stop_id} was not seen during training.')

    hours_str, minutes_str, seconds_str = arrival_time.split(':')
    minutes = int(hours_str) * 60 + int(minutes_str) + int(seconds_str) / 60.0
    return np.array([[stop_mapping[stop_id], minutes, int(stop_sequence)]], dtype=np.float32)


def predict_guide(model, stop_id, arrival_time, stop_sequence, stop_mapping, guide_lookup, current_stop=None, destination=None, current_time=None):
    feature = build_feature_vector(stop_id, arrival_time, stop_sequence, stop_mapping)

    probabilities = model.predict(feature, verbose=0)[0]
    predicted_index = int(np.argmax(probabilities))
    suggestion = guide_lookup.get(predicted_index, guide_lookup.get(0))
    contextual = build_contextual_guide(suggestion, current_stop=current_stop, destination=destination, current_time=current_time)
    return contextual, float(probabilities[predicted_index])


def predict_route(model, stop_id, arrival_time, stop_sequence, stop_mapping, guide_lookup, current_stop=None, destination=None, current_time=None):
    suggestion, confidence = predict_guide(
        model,
        stop_id,
        arrival_time,
        stop_sequence,
        stop_mapping,
        guide_lookup,
        current_stop=current_stop,
        destination=destination,
        current_time=current_time,
    )
    return suggestion.get('guide_text', str(suggestion)), confidence
