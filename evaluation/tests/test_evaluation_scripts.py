#!/usr/bin/env python3
"""
Unit tests covering evaluate_wer and jev_benchmark.
"""

import unittest
import os
import sys

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "scripts")))
from evaluate_wer import calculate_wer
from jev_benchmark import mock_jev_decision


class TestEvaluationScripts(unittest.TestCase):

    def test_wer_exact_match(self):
        ref = "emergency in warehouse 3"
        hyp = "emergency in warehouse 3"
        wer, edits, _, _, words = calculate_wer(ref, hyp)
        self.assertEqual(wer, 0.0)
        self.assertEqual(edits, 0)
        self.assertEqual(words, 4)

    def test_wer_substitution_and_insertion(self):
        ref = "fire in electrical room"
        hyp = "wire in big electrical room"
        wer, edits, _, _, words = calculate_wer(ref, hyp)
        self.assertGreater(wer, 0.0)
        self.assertEqual(words, 4)

    def test_jev_mock_oracle(self):
        case = {
            "ground_truth": {
                "incident_type": "Chemical Spill",
                "severity_level": "High"
            }
        }
        res = mock_jev_decision(case)
        self.assertEqual(res["incident_type"], "Chemical Spill")
        self.assertEqual(res["severity_level"], "High")
        self.assertGreater(res["confidence"], 0.9)


if __name__ == "__main__":
    unittest.main()
