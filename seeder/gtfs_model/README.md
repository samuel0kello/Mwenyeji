# GTFS Model

A machine learning model for processing and extracting GTFS (General Transit Feed Specification) data. This model is trained to understand and classify transit route information.

## Overview

This directory contains the GTFS (General Transit Feed Specification) machine learning model used for the Mwenyeji guide suggestion engine. The model processes transit route data and helps extract relevant information for better route suggestions.

## Model Components

- **train_model.py** - Training script for the GTFS model
- **infer_model.py** - Model inference for making predictions
- **data_loader.py** - Data loading and preprocessing utilities
- **export_model.py** - Export model to various formats (TensorFlow Lite, etc.)

## Features

- Processes GTFS transit data
- Extracts route and stop mappings
- Supports TensorFlow Lite export for mobile deployment
- Handles route classification and suggestion tasks

## Usage

### Training
```bash
python train_model.py --data-path <path-to-data> --epochs 50
```

### Inference
```bash
python infer_model.py --model-path <path-to-model> --input <path-to-input>
```

### Export to Mobile Format
```bash
python export_model.py --model-path <path-to-model> --output-format tflite
```

## Model Details

- **Framework**: TensorFlow/Keras
- **Input**: GTFS route and stop data
- **Output**: Route classifications and suggestions

## Integration

The trained model is integrated into the Android application through:
- TensorFlow Lite conversion for mobile inference
- Stop and route mappings in `stop_mappings.json` and route mappings
- Guide suggestion engine in the core ML module

## Dependencies

See `requirements.txt` for Python dependencies.

```
tensorflow>=2.12.0
pandas>=1.5.0
scikit-learn>=1.0.0
numpy>=1.21.0
```

## Output Artifacts

- **mwenyeji_route_model.keras** - Trained Keras model
- **mwenyeji_route_model.tflite** - TensorFlow Lite model for mobile
- **stop_mappings.json** - Stop ID to name mappings
- **route_mappings.json** - Route ID to name mappings
