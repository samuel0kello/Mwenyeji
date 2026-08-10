import csv
import tempfile
import unittest
from pathlib import Path

from gtfs_model.data_loader import load_gtfs_data


class LoadGtfsDataTests(unittest.TestCase):
    def test_load_gtfs_data_parses_basic_files(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmpdir = Path(tmpdir)

            with (tmpdir / 'trips.txt').open('w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['trip_id', 'route_id'])
                writer.writerow(['t1', 'r1'])
                writer.writerow(['t2', 'r2'])

            with (tmpdir / 'stop_times.txt').open('w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['trip_id', 'arrival_time', 'departure_time', 'stop_id', 'stop_sequence'])
                writer.writerow(['t1', '06:00:00', '06:00:20', 's1', '1'])
                writer.writerow(['t1', '06:10:00', '06:10:20', 's2', '2'])
                writer.writerow(['t2', '07:00:00', '07:00:20', 's3', '1'])

            features, labels, stop_to_id, route_to_id, guide_catalog = load_gtfs_data(
                stop_times_path=tmpdir / 'stop_times.txt',
                trips_path=tmpdir / 'trips.txt',
            )

            self.assertEqual(features.shape[1], 3)
            self.assertEqual(labels[0], labels[1])
            self.assertEqual(stop_to_id['s1'], 0)
            self.assertIn(route_to_id['r1'], labels)
            self.assertIn(route_to_id['r2'], labels)
            self.assertGreaterEqual(len(guide_catalog), 1)


if __name__ == '__main__':
    unittest.main()
