import csv
import hashlib
import json
from pathlib import Path

import numpy as np


def _parse_arrival_minutes(arrival_time: str) -> float:
    hours_str, minutes_str, seconds_str = arrival_time.split(':')
    hours = int(hours_str)
    minutes = int(minutes_str)
    seconds = int(seconds_str)
    return (hours * 60) + minutes + (seconds / 60.0)


def _extract_guide_narrative(row: dict) -> str:
    for key in ('guideNarrative', 'guideText', 'stepwiseGuide', 'stepsText'):
        value = (row.get(key) or '').strip()
        if value:
            return value

    steps_value = (row.get('steps') or '').strip()
    if steps_value:
        try:
            parsed_steps = json.loads(steps_value)
            if isinstance(parsed_steps, list):
                return ' | '.join(str(item) for item in parsed_steps if str(item).strip())
        except json.JSONDecodeError:
            return steps_value

    best_time = (row.get('bestTimeOfDay') or '').strip() or 'ANYTIME'
    timing_reason = (row.get('timingReason') or '').strip() or 'No specific timing note.'
    warnings = (row.get('warnings') or '').strip() or 'No extra warnings.'
    fare_ksh = (row.get('fareKsh') or '').strip() or 'N/A'
    return f"Best at {best_time}: {timing_reason}. Fare around {fare_ksh} KSh. Avoid: {warnings}."


def _build_guide_summary(row: dict) -> dict:
    best_time = (row.get('bestTimeOfDay') or '').strip() or 'ANYTIME'
    timing_reason = (row.get('timingReason') or '').strip() or 'No specific timing note.'
    warnings = (row.get('warnings') or '').strip() or 'No extra warnings.'
    tags = (row.get('tags') or '').strip() or 'GENERAL'
    fare_ksh = (row.get('fareKsh') or '').strip() or 'N/A'
    steps_count = (row.get('stepsCount') or '').strip() or '1'
    guide_narrative = _extract_guide_narrative(row)

    summary = f"Best at {best_time}: {timing_reason} | fare≈{fare_ksh} KSh | tags: {tags}"
    return {
        'route_id': (row.get('routeId') or '').strip(),
        'summary': summary,
        'details': {
            'best_time': best_time,
            'timing_reason': timing_reason,
            'warnings': warnings,
            'tags': tags,
            'fare_ksh': fare_ksh,
            'steps_count': steps_count,
            'guide_narrative': guide_narrative,
        },
    }


def load_guide_catalog(guide_csv_path=None):
    workspace = Path(__file__).resolve().parent.parent
    guide_catalog_path = Path(guide_csv_path) if guide_csv_path else workspace / 'cleaned_user_guides.csv'
    guide_catalog_path = Path(guide_catalog_path)

    guide_catalog = []
    if guide_catalog_path.exists():
        with guide_catalog_path.open('r', encoding='utf-8') as handle:
            reader = csv.DictReader(handle)
            for row in reader:
                suggestion = _build_guide_summary(row)
                if suggestion['route_id']:
                    guide_catalog.append(suggestion)

    if not guide_catalog:
        guide_catalog.append({
            'route_id': 'fallback',
            'summary': 'Use the usual route plan and watch for crowding.',
            'details': {
                'best_time': 'ANYTIME',
                'timing_reason': 'Fallback guide suggestion.',
                'warnings': 'No extra warnings.',
                'tags': 'GENERAL',
                'fare_ksh': 'N/A',
                'steps_count': '1',
            },
        })

    for index, suggestion in enumerate(guide_catalog):
        suggestion['index'] = index

    return guide_catalog


def _guide_index_for_route(route_id: str, guide_catalog: list) -> int:
    if not guide_catalog:
        return 0

    for index, suggestion in enumerate(guide_catalog):
        if suggestion.get('route_id') == route_id:
            return index

    digest = hashlib.sha256(route_id.encode('utf-8')).hexdigest()
    return int(digest[:8], 16) % len(guide_catalog)


def build_contextual_guide(guide_entry: dict, current_stop=None, destination=None, current_time=None):
    details = guide_entry.get('details', {}) if isinstance(guide_entry, dict) else {}
    narrative = details.get('guide_narrative') or guide_entry.get('summary', '')
    warning = details.get('warnings') or 'No extra warnings.'
    best_time = details.get('best_time') or 'ANYTIME'

    start_hint = current_stop or 'your current stage'
    destination_hint = destination or 'your destination stage'
    time_hint = current_time or 'now'

    guide_text = narrative
    if 'Board at' not in guide_text and 'board' not in guide_text.lower():
        guide_text = f"Board at {start_hint}. Alight at {destination_hint}. Best time {best_time} for this journey. {warning}"

    stepwise_guide = []
    if narrative:
        stepwise_guide.append({'step': 1, 'text': f"Board at {start_hint} and wait for the matatu heading to {destination_hint}."})
        stepwise_guide.append({'step': 2, 'text': f"Follow the local advice: {narrative}"})
        stepwise_guide.append({'step': 3, 'text': f"Alight at {destination_hint} when you reach the stop closest to your destination."})
        if warning and warning != 'No extra warnings.':
            stepwise_guide.append({'step': 4, 'text': f"Avoid this: {warning}"})
    else:
        stepwise_guide.append({'step': 1, 'text': f"Board at {start_hint} at about {time_hint}."})
        stepwise_guide.append({'step': 2, 'text': f"Ride until you are near {destination_hint}."})
        stepwise_guide.append({'step': 3, 'text': f"Alight at {destination_hint} and use the nearest landmark to confirm you are in the right place."})

    return {
        'guide_text': guide_text,
        'stepwise_guide': stepwise_guide,
        'boarding_hint': f"Board at {start_hint}.",
        'alighting_hint': f"Alight at {destination_hint}.",
        'landmark_hint': 'Look for a well-known landmark or the nearest stage sign before stepping off.',
    }


def load_gtfs_data(stop_times_path, trips_path, guide_csv_path=None):
    stop_times_path = Path(stop_times_path)
    trips_path = Path(trips_path)
    guide_catalog = load_guide_catalog(guide_csv_path)

    trip_to_route = {}
    with trips_path.open('r', encoding='utf-8') as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            trip_id = (row.get('trip_id') or '').strip()
            route_id = (row.get('route_id') or '').strip()
            if trip_id and route_id:
                trip_to_route[trip_id] = route_id

    stop_to_id = {}
    route_to_id = {}
    features = []
    labels = []

    with stop_times_path.open('r', encoding='utf-8') as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            trip_id = (row.get('trip_id') or '').strip()
            arrival_time = (row.get('arrival_time') or '').strip()
            stop_id = (row.get('stop_id') or '').strip()
            stop_sequence = (row.get('stop_sequence') or '').strip()

            if not trip_id or not arrival_time or not stop_id:
                continue

            if stop_id not in stop_to_id:
                stop_to_id[stop_id] = len(stop_to_id)

            route_id = trip_to_route.get(trip_id, trip_id)
            if route_id not in route_to_id:
                route_to_id[route_id] = _guide_index_for_route(route_id, guide_catalog)

            try:
                minutes = _parse_arrival_minutes(arrival_time)
            except ValueError:
                continue

            try:
                sequence = int(stop_sequence)
            except ValueError:
                sequence = 0

            features.append([stop_to_id[stop_id], minutes, sequence])
            labels.append(route_to_id[route_id])

    if not features:
        raise ValueError('No usable data rows were parsed. Check your GTFS file format and headers.')

    features_array = np.array(features, dtype=np.float32)
    labels_array = np.array(labels, dtype=np.int32)

    return features_array, labels_array, stop_to_id, route_to_id, guide_catalog
