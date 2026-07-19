import argparse
from pathlib import Path

from gtfs_model.export_model import bundle_android_artifacts


def parse_args():
    parser = argparse.ArgumentParser(description='Export the trained guide model for Android on-device inference.')
    parser.add_argument('--model-path', default='mwenyeji_route_model.keras', help='Path to the trained Keras model file.')
    parser.add_argument('--stop-mappings', default='stop_mappings.json', help='Path to the stop mapping JSON file.')
    parser.add_argument('--guide-suggestions', default='guide_suggestions.json', help='Path to the guide suggestion JSON file.')
    parser.add_argument('--output-dir', default='android_model_bundle', help='Directory for the Android-ready artifact bundle.')
    return parser.parse_args()


def main():
    args = parse_args()
    workspace = Path(__file__).resolve().parent

    model_path = Path(args.model_path)
    if not model_path.is_absolute():
        model_path = workspace / model_path

    stop_mapping_path = Path(args.stop_mappings)
    if not stop_mapping_path.is_absolute():
        stop_mapping_path = workspace / stop_mapping_path

    guide_suggestions_path = Path(args.guide_suggestions)
    if not guide_suggestions_path.is_absolute():
        guide_suggestions_path = workspace / guide_suggestions_path

    output_dir = Path(args.output_dir)
    if not output_dir.is_absolute():
        output_dir = workspace / output_dir

    bundle_android_artifacts(model_path, stop_mapping_path, guide_suggestions_path, output_dir)
    print(f'Exported Android bundle to {output_dir}')


if __name__ == '__main__':
    main()
