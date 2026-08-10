import tempfile
import unittest
from pathlib import Path

from tensorflow import keras

from gtfs_model.export_model import export_tflite_model


class ExportModelTests(unittest.TestCase):
    def test_export_tflite_model_writes_binary_artifact(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmpdir = Path(tmpdir)
            model_path = tmpdir / 'tiny_model.keras'
            output_path = tmpdir / 'tiny_model.tflite'

            model = keras.Sequential([
                keras.layers.Input(shape=(3,)),
                keras.layers.Dense(4, activation='relu'),
                keras.layers.Dense(2, activation='softmax'),
            ])
            model.save(model_path)

            export_tflite_model(model_path, output_path)

            self.assertTrue(output_path.exists())
            self.assertGreater(output_path.stat().st_size, 0)


if __name__ == '__main__':
    unittest.main()
