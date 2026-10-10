#!/usr/bin/env python3
"""
Unit tests verifying LLM extraction contract, parsing resilience, and null-preservation rules.
"""

import unittest
import json
import os
import sys

# Add scripts directory to path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "scripts")))
from evaluate_extraction import sanitize_and_parse_json, evaluate_case, run_benchmark


class TestExtractionContract(unittest.TestCase):

    def test_markdown_code_fence_sanitization(self):
        raw = """```json
        {
          "incident_type": "Fire / Explosion",
          "severity_level": "High"
        }
        ```"""
        parsed = sanitize_and_parse_json(raw)
        self.assertEqual(parsed["incident_type"], "Fire / Explosion")
        self.assertEqual(parsed["severity_level"], "High")

    def test_surrounding_conversational_text_stripped(self):
        raw = """Certainly! Here is the JSON:
        {
          "incident_location": "Bay 3"
        }
        Let me know if you need more details."""
        parsed = sanitize_and_parse_json(raw)
        self.assertEqual(parsed["incident_location"], "Bay 3")

    def test_null_preservation_rule(self):
        case = {
            "ground_truth": {
                "incident_type": "Chemical Spill",
                "severity_level": None
            },
            "must_be_null": ["severity_level", "witness_information"]
        }
        # Model correctly omitted severity_level and witness_information
        extracted_correct = {
            "incident_type": "Chemical Spill"
        }
        tp, fp, fn, null_viols = evaluate_case(case, extracted_correct)
        self.assertEqual(tp, 1)
        self.assertEqual(null_viols, 0)

        # Model hallucinated severity_level
        extracted_hallucinated = {
            "incident_type": "Chemical Spill",
            "severity_level": "Medium"
        }
        tp, fp, fn, null_viols = evaluate_case(case, extracted_hallucinated)
        self.assertEqual(null_viols, 1)
        self.assertEqual(fp, 1)

    def test_benchmark_runner_against_dataset(self):
        dataset_path = os.path.join(os.path.dirname(__file__), "..", "dataset", "incident_test_cases.json")
        results = run_benchmark(dataset_path, simulated=True)
        self.assertEqual(results["total_cases"], 5)
        self.assertEqual(results["null_preservation_rate"], 1.0)
        self.assertEqual(results["total_null_violations"], 0)


if __name__ == "__main__":
    unittest.main()
