import unittest

import numpy as np

from gtfs_model.infer_model import build_feature_vector


class InferModelTests(unittest.TestCase):
    def test_build_feature_vector_converts_time_and_sequence(self):
        stop_mapping = {'stop_a': 2}

        feature = build_feature_vector('stop_a', '01:02:03', 4, stop_mapping)

        self.assertEqual(feature.shape, (1, 3))
        np.testing.assert_allclose(feature[0, 0], 2.0)
        np.testing.assert_allclose(feature[0, 1], 62.05)
        np.testing.assert_allclose(feature[0, 2], 4.0)


if __name__ == '__main__':
    unittest.main()
