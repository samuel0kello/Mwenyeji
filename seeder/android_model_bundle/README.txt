Place guide_suggester.tflite in your Android app's assets/models folder.

This model expects a 3-feature input vector:
1. stop_id_index
2. arrival_minutes
3. stop_sequence

The app should map the stop ID to the index from stop_mappings.json and then run inference on the model.
