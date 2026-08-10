import csv
import tempfile
import unittest
from pathlib import Path

from gtfs_model.data_loader import build_contextual_guide, load_gtfs_data


class GuideSuggestionTests(unittest.TestCase):
    def test_load_gtfs_data_returns_guide_catalog(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmpdir = Path(tmpdir)

            with (tmpdir / 'trips.txt').open('w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['trip_id', 'route_id'])
                writer.writerow(['t1', 'r1'])

            with (tmpdir / 'stop_times.txt').open('w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['trip_id', 'arrival_time', 'departure_time', 'stop_id', 'stop_sequence'])
                writer.writerow(['t1', '06:00:00', '06:00:20', 's1', '1'])

            with (tmpdir / 'guides.csv').open('w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['routeId', 'bestTimeOfDay', 'timingReason', 'warnings', 'tags', 'fareKsh', 'guideNarrative'])
                writer.writerow(['r1', 'MIDDAY', 'quick ride', 'avoid traffic', 'RELIABLE', '30', 'Board at Kawangware stage. Alight at CBD stage. Avoid the matatu that turns at the highway. Landmark: the big blue church.'])

            features, labels, stop_to_id, route_to_id, guide_catalog = load_gtfs_data(
                stop_times_path=tmpdir / 'stop_times.txt',
                trips_path=tmpdir / 'trips.txt',
                guide_csv_path=tmpdir / 'guides.csv',
            )

            self.assertEqual(features.shape[0], 1)
            self.assertEqual(labels.shape[0], 1)
            self.assertIn('r1', route_to_id)
            self.assertGreaterEqual(len(guide_catalog), 1)
            self.assertIn('MIDDAY', guide_catalog[0]['summary'])
            contextual = build_contextual_guide(
                guide_catalog[0],
                current_stop='Kawangware',
                destination='CBD',
                current_time='08:00',
            )
            self.assertIn('Board at', contextual['guide_text'])
            self.assertIn('Alight at', contextual['guide_text'])


if __name__ == '__main__':
    unittest.main()
