import argparse
import json
from pathlib import Path

from gtfs_model.infer_model import load_artifacts, predict_guide


def parse_args():
    parser = argparse.ArgumentParser(description='Suggest a guide entry from GTFS stop/time features.')
    parser.add_argument('--stop-id', default=None, help='Stop ID from the training mappings.')
    parser.add_argument('--arrival-time', default='08:00:00', help='Arrival time in HH:MM:SS format.')
    parser.add_argument('--stop-sequence', type=int, default=1, help='Stop sequence number for the trip.')
    parser.add_argument('--current-stop', default='your current stage', help='Where the rider is starting from.')
    parser.add_argument('--destination', default='your destination stage', help='Where the rider is heading to.')
    parser.add_argument('--current-time', default='now', help='The time of travel for context.')
    parser.add_argument('--model-path', default='mwenyeji_route_model.keras', help='Path to the trained model file.')
    parser.add_argument('--stop-mappings', default='stop_mappings.json', help='Path to the stop mapping JSON file.')
    parser.add_argument('--guide-mappings', default='guide_suggestions.json', help='Path to the guide suggestion JSON file.')
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

    guide_mapping_path = Path(args.guide_mappings)
    if not guide_mapping_path.is_absolute():
        guide_mapping_path = workspace / guide_mapping_path

    model, stop_mapping, guide_lookup = load_artifacts(model_path, stop_mapping_path, guide_mapping_path)

    stop_id = args.stop_id or next(iter(stop_mapping.keys()))
    suggestion, confidence = predict_guide(
        model=model,
        stop_id=stop_id,
        arrival_time=args.arrival_time,
        stop_sequence=args.stop_sequence,
        stop_mapping=stop_mapping,
        guide_lookup=guide_lookup,
        current_stop=args.current_stop,
        destination=args.destination,
        current_time=args.current_time,
    )

    print(json.dumps({
        'stop_id': stop_id,
        'arrival_time': args.arrival_time,
        'stop_sequence': args.stop_sequence,
        'guide_suggestion': suggestion.get('guide_text', ''),
        'stepwise_guide': suggestion.get('stepwise_guide', []),
        'boarding_hint': suggestion.get('boarding_hint', ''),
        'alighting_hint': suggestion.get('alighting_hint', ''),
        'landmark_hint': suggestion.get('landmark_hint', ''),
        'confidence': round(confidence, 4),
    }, indent=2))


if __name__ == '__main__':
    main()
