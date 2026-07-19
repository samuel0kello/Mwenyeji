import json
from pathlib import Path

import tensorflow as tf
from tensorflow import keras
from tensorflow import lite as tflite


def export_tflite_model(model_path, output_path):
    model_path = Path(model_path)
    output_path = Path(output_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    model = keras.models.load_model(str(model_path), compile=False)
    concrete_func = tf.function(
        lambda x: model(x),
        input_signature=[tf.TensorSpec(shape=[None, 3], dtype=tf.float32, name='input_1')],
    ).get_concrete_function()

    converter = tflite.TFLiteConverter.from_concrete_functions([concrete_func])
    converter.experimental_new_converter = False
    converter.target_spec.supported_ops = [tflite.OpsSet.TFLITE_BUILTINS]
    converter.optimizations = [tflite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    output_path.write_bytes(tflite_model)
    return output_path


def bundle_android_artifacts(model_path, stop_mapping_path, guide_suggestions_path, output_dir):
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    tflite_path = export_tflite_model(model_path, output_dir / 'guide_suggester.tflite')

    with Path(stop_mapping_path).open('r', encoding='utf-8') as handle:
        stop_mapping = json.load(handle)

    with Path(guide_suggestions_path).open('r', encoding='utf-8') as handle:
        guide_suggestions = json.load(handle)

    manifest = {
        'model_file': tflite_path.name,
        'input_features': ['stop_id_index', 'arrival_minutes', 'stop_sequence'],
        'output_labels': [entry.get('summary', '') for entry in guide_suggestions],
        'stop_mapping': stop_mapping,
    }

    with (output_dir / 'model_manifest.json').open('w', encoding='utf-8') as handle:
        json.dump(manifest, handle, indent=2)

    return output_dir
