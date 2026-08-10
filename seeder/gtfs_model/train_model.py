import json
from pathlib import Path

import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow import keras
from tensorflow.keras import layers

from .data_loader import load_gtfs_data
from .export_model import export_tflite_model


def train_and_save_model(stop_times_path, trips_path, output_dir='.', guide_csv_path=None):
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    features, labels, stop_to_id, route_to_id, guide_catalog = load_gtfs_data(
        stop_times_path=stop_times_path,
        trips_path=trips_path,
        guide_csv_path=guide_csv_path,
    )

    unique_labels = np.unique(labels)
    if len(unique_labels) < 2:
        raise ValueError('Need at least two guide labels to train a classifier.')

    X_train, X_val, y_train, y_val = train_test_split(
        features,
        labels,
        test_size=0.2,
        random_state=42,
        stratify=labels,
    )

    num_classes = len(guide_catalog)
    if num_classes < 2:
        raise ValueError('Need at least two guide suggestions to train a classifier.')

    model = keras.Sequential([
        layers.Input(shape=(3,)),
        layers.Dense(128, activation='relu'),
        layers.Dense(64, activation='relu'),
        layers.Dropout(0.2),
        layers.Dense(num_classes, activation='softmax'),
    ])

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=1e-3),
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy'],
    )

    history = model.fit(
        X_train,
        y_train,
        validation_data=(X_val, y_val),
        epochs=15,
        batch_size=64,
        verbose=1,
    )

    model_path = output_dir / 'mwenyeji_route_model.keras'
    model.save(model_path)
    export_tflite_model(model_path, output_dir / 'mwenyeji_route_model.tflite')

    with (output_dir / 'stop_mappings.json').open('w', encoding='utf-8') as handle:
        json.dump({'stop_id_to_index': stop_to_id}, handle, indent=2)

    with (output_dir / 'route_mappings.json').open('w', encoding='utf-8') as handle:
        json.dump({'route_id_to_index': route_to_id}, handle, indent=2)

    with (output_dir / 'guide_suggestions.json').open('w', encoding='utf-8') as handle:
        json.dump(guide_catalog, handle, indent=2)

    return {
        'model_path': str(model_path),
        'history': history.history,
        'num_classes': num_classes,
    }
