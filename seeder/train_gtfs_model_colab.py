import csv
import json
import os
from pathlib import Path

import numpy as np

try:
    import tensorflow as tf
    from tensorflow import keras
    from tensorflow.keras import layers
except ImportError as exc:
    raise SystemExit(
        "TensorFlow is not installed. In Colab run: !pip install -q tensorflow numpy pandas scikit-learn"
    ) from exc

try:
    from sklearn.model_selection import train_test_split
except ImportError as exc:
    raise SystemExit(
        "scikit-learn is not installed. In Colab run: !pip install -q scikit-learn"
    ) from exc


def upload_files_if_needed():
    """Upload stop_times.txt and trips.txt from Colab when files are missing."""
    if not os.path.exists('stop_times.txt') or not os.path.exists('trips.txt'):
        try:
            from google.colab import files
        except Exception:
            print('No Colab environment detected; expecting local files in the current folder.')
            return False

        print('Upload stop_times.txt and trips.txt when prompted.')
        uploaded = files.upload()
        for name in uploaded:
            if name.endswith('.txt'):
                os.rename(name, name)
        return os.path.exists('stop_times.txt') and os.path.exists('trips.txt')
    return True


def load_gtfs_data(stop_times_path='stop_times.txt', trips_path='trips.txt'):
    # Load trip -> route mapping
    trip_to_route = {}
    with open(trips_path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            trip_id = row.get('trip_id')
            route_id = row.get('route_id')
            if trip_id and route_id:
                trip_to_route[trip_id] = route_id

    stop_to_id = {}
    trip_to_label = {}
    route_to_id = {}

    features = []
    labels = []

    with open(stop_times_path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            trip_id = row.get('trip_id', '').strip()
            arrival_time = row.get('arrival_time', '').strip()
            stop_id = row.get('stop_id', '').strip()
            stop_sequence = row.get('stop_sequence', '').strip()

            if not trip_id or not arrival_time or not stop_id:
                continue

            if stop_id not in stop_to_id:
                stop_to_id[stop_id] = len(stop_to_id)

            if trip_id not in trip_to_label:
                trip_to_label[trip_id] = len(trip_to_label)

            route_id = trip_to_route.get(trip_id, trip_id)
            if route_id not in route_to_id:
                route_to_id[route_id] = len(route_to_id)

            try:
                hh, mm, ss = arrival_time.split(':')
                minutes = int(hh) * 60 + int(mm) + int(ss) / 60.0
            except ValueError:
                continue

            try:
                seq = int(stop_sequence)
            except ValueError:
                seq = 0

            features.append([stop_to_id[stop_id], minutes, seq])
            labels.append(route_to_id[route_id])

    if not features:
        raise ValueError('No usable records were parsed. Check your CSV headers and file contents.')

    X = np.array(features, dtype=np.float32)
    y = np.array(labels, dtype=np.int32)

    print(f'Parsed {len(X)} rows from stop_times.txt')
    print(f'Unique stops: {len(stop_to_id)}')
    print(f'Unique routes: {len(route_to_id)}')
    return X, y, stop_to_id, route_to_id


def train_model(X, y, stop_to_id, route_to_id, output_dir='.'):
    X_train, X_val, y_train, y_val = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y if len(np.unique(y)) > 1 else None,
    )

    num_classes = len(np.unique(y))
    model = keras.Sequential([
        layers.Input(shape=(3,)),
        layers.Dense(128, activation='relu'),
        layers.Dense(64, activation='relu'),
        layers.Dropout(0.2),
        layers.Dense(num_classes, activation='softmax')
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

    model.save(os.path.join(output_dir, 'mwenyeji_route_model.keras'))
    with open(os.path.join(output_dir, 'stop_mappings.json'), 'w', encoding='utf-8') as f:
        json.dump({'stop_id_to_index': stop_to_id}, f, indent=2)
    with open(os.path.join(output_dir, 'route_mappings.json'), 'w', encoding='utf-8') as f:
        json.dump({'route_id_to_index': route_to_id}, f, indent=2)

    print('Training complete.')
    print('Saved model to:', os.path.join(output_dir, 'mwenyeji_route_model.keras'))
    return model, history


if __name__ == '__main__':
    upload_files_if_needed()
    X, y, stop_to_id, route_to_id = load_gtfs_data()
    train_model(X, y, stop_to_id, route_to_id, output_dir='.')
