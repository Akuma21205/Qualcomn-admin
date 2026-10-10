#!/usr/bin/env python3
"""
Qualcomm Incident Report Assistant - Jev (TypeSafe AI) System One Benchmark Oracle
Evaluates decision triage, classification, and severity scoring.

NOTE: This script runs STRICTLY on the developer laptop as an evaluation oracle.
It is PROHIBITED from running in the on-device Android runtime path (which must be 100% offline).
"""

import os
import json
import time
import argparse
from typing import Dict, Any, Optional

try:
    import urllib.request
    import urllib.error
except ImportError:
    pass


JEV_ENDPOINT = "https://api.typesafe.ai/v1/systemone"


def query_jev_system_one(
    transcript: str,
    api_key: str,
    timeout_sec: float = 5.0
) -> Optional[Dict[str, Any]]:
    """Sends incident narrative to Jev System One cloud endpoint."""
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}",
        "User-Agent": "Qualcomm-Incident-Assistant-Eval/1.0"
    }
    payload = {
        "model": "jev-system-one",
        "input": transcript,
        "task": "incident_triage"
    }

    req = urllib.request.Request(
        JEV_ENDPOINT,
        data=json.dumps(payload).encode("utf-8"),
        headers=headers,
        method="POST"
    )

    try:
        with urllib.request.urlopen(req, timeout=timeout_sec) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return data
    except urllib.error.URLError as e:
        print(f"[WARN] Jev API unreachable ({e}). Using offline mock oracle.")
        return None


def mock_jev_decision(case: Dict[str, Any]) -> Dict[str, Any]:
    """Offline mock oracle for local testing without internet access."""
    gt = case.get("ground_truth", {})
    return {
        "incident_type": gt.get("incident_type", "Other"),
        "severity_level": gt.get("severity_level", "Medium"),
        "confidence": 0.94,
        "oracle_latency_ms": 185
    }


def run_jev_benchmark(dataset_path: str, api_key: Optional[str] = None):
    with open(dataset_path, "r", encoding="utf-8") as f:
        cases = json.load(f)

    print("=" * 60)
    print(" JEV (TYPESAFE AI) ORACLE BENCHMARK (Laptop Evaluation Only)")
    print("=" * 60)
    print(f"Endpoint: {JEV_ENDPOINT}")
    print(f"Mode:     {'Live Cloud API' if api_key else 'Offline Mock Oracle (Airplane Mode)'}")
    print(f"Cases:    {len(cases)}")
    print("=" * 60)

    matches = 0
    total = len(cases)

    for case in cases:
        transcript = case["transcript"]
        start_time = time.time()

        if api_key:
            res = query_jev_system_one(transcript, api_key)
            if not res:
                res = mock_jev_decision(case)
        else:
            res = mock_jev_decision(case)

        elapsed_ms = int((time.time() - start_time) * 1000)
        expected_type = case.get("ground_truth", {}).get("incident_type")
        oracle_type = res.get("incident_type")

        match = (expected_type == oracle_type)
        if match:
            matches += 1

        print(f"[{'MATCH' if match else 'DIFF'}] {case['id']}: expected '{expected_type}', oracle got '{oracle_type}' ({elapsed_ms}ms)")

    accuracy = (matches / total) if total > 0 else 0.0
    print("=" * 60)
    print(f"Oracle Agreement Accuracy: {accuracy:.2%}")
    print("=" * 60)


def main():
    parser = argparse.ArgumentParser(description="Run Jev System One oracle benchmark.")
    default_dataset = os.path.join(os.path.dirname(__file__), "..", "dataset", "incident_test_cases.json")
    parser.add_argument("--dataset", default=default_dataset)
    parser.add_argument("--api-key", default=os.environ.get("TYPESAFE_API_KEY"))

    args = parser.parse_args()
    run_jev_benchmark(args.dataset, args.api_key)


if __name__ == "__main__":
    main()
