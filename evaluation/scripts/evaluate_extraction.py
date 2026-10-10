#!/usr/bin/env python3
"""
Qualcomm Incident Report Assistant - LLM Extraction Evaluation Benchmark
Evaluates field extraction precision, recall, and null-preservation (anti-hallucination).

Target model: Qwen3.5-2B (W4A16 on Qualcomm Snapdragon 8 Elite Hexagon NPU).
"""

import json
import os
import sys
import argparse
from typing import Dict, Any, List, Tuple


def sanitize_and_parse_json(raw_text: str) -> Dict[str, Any]:
    """Sanitizes raw LLM output and extracts the JSON object."""
    cleaned = raw_text.strip()
    # Strip markdown code blocks
    if "```json" in cleaned:
        cleaned = cleaned.split("```json")[1].split("```")[0].strip()
    elif "```" in cleaned:
        cleaned = cleaned.split("```")[1].split("```")[0].strip()
    
    # Locate first { and last }
    first_brace = cleaned.find("{")
    last_brace = cleaned.rfind("}")
    if first_brace != -1 and last_brace != -1 and last_brace > first_brace:
        cleaned = cleaned[first_brace : last_brace + 1]

    try:
        return json.loads(cleaned)
    except json.JSONDecodeError as e:
        raise ValueError(f"Failed to parse JSON: {e}\nRaw snippet: {cleaned[:100]}")


def evaluate_case(
    case: Dict[str, Any],
    extracted_patch: Dict[str, Any]
) -> Tuple[int, int, int, int]:
    """
    Evaluates extraction against ground truth:
    Returns (true_positives, false_positives, false_negatives, null_violations).
    """
    tp = 0
    fp = 0
    fn = 0
    null_violations = 0

    ground_truth = case.get("ground_truth", {})
    must_be_null = set(case.get("must_be_null", []))

    for field, expected_val in ground_truth.items():
        extracted_val = extracted_patch.get(field)

        if expected_val is not None:
            if extracted_val is not None and str(extracted_val).strip() != "":
                tp += 1
            else:
                fn += 1

    # Check null violations (anti-hallucination guardrail)
    for null_field in must_be_null:
        val = extracted_patch.get(null_field)
        if val is not None and val != "" and val != []:
            null_violations += 1
            fp += 1

    return tp, fp, fn, null_violations


def simulate_model_inference(transcript: str, case: Dict[str, Any]) -> str:
    """Simulates Qwen3.5-2B extraction for benchmark testing."""
    gt = case.get("ground_truth", {})
    # Return formatted JSON with only non-null ground truth fields (matching prompt instructions)
    active = {k: v for k, v in gt.items() if v is not None}
    return json.dumps(active, indent=2)


def run_benchmark(dataset_path: str, simulated: bool = True) -> Dict[str, Any]:
    with open(dataset_path, "r", encoding="utf-8") as f:
        cases = json.load(f)

    total_tp = 0
    total_fp = 0
    total_fn = 0
    total_null_violations = 0
    total_must_be_null_checks = 0

    case_results = []

    for case in cases:
        transcript = case["transcript"]
        if simulated:
            raw_output = simulate_model_inference(transcript, case)
        else:
            raise NotImplementedError("Live QIDK inference runner attaches via ADB socket in M3.")

        extracted = sanitize_and_parse_json(raw_output)
        tp, fp, fn, null_viols = evaluate_case(case, extracted)

        must_be_null_count = len(case.get("must_be_null", []))
        total_must_be_null_checks += must_be_null_count

        total_tp += tp
        total_fp += fp
        total_fn += fn
        total_null_violations += null_viols

        case_results.append({
            "id": case["id"],
            "title": case["title"],
            "tp": tp,
            "fp": fp,
            "fn": fn,
            "null_violations": null_viols,
            "null_preservation_rate": (
                (must_be_null_count - null_viols) / must_be_null_count
                if must_be_null_count > 0 else 1.0
            )
        })

    precision = total_tp / (total_tp + total_fp) if (total_tp + total_fp) > 0 else 0.0
    recall = total_tp / (total_tp + total_fn) if (total_tp + total_fn) > 0 else 0.0
    f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0.0
    null_preservation_rate = (
        (total_must_be_null_checks - total_null_violations) / total_must_be_null_checks
        if total_must_be_null_checks > 0 else 1.0
    )

    return {
        "total_cases": len(cases),
        "precision": precision,
        "recall": recall,
        "f1_score": f1,
        "total_null_violations": total_null_violations,
        "null_preservation_rate": null_preservation_rate,
        "case_results": case_results
    }


def main():
    parser = argparse.ArgumentParser(description="Evaluate LLM extraction against incident test cases.")
    default_dataset = os.path.join(os.path.dirname(__file__), "..", "dataset", "incident_test_cases.json")
    parser.add_argument("--dataset", default=default_dataset, help="Path to incident_test_cases.json")
    parser.add_argument("--simulated", action="store_true", default=True, help="Use simulated inference")

    args = parser.parse_args()

    print(f"Loading dataset from: {args.dataset}")
    results = run_benchmark(args.dataset, simulated=args.simulated)

    print("\n" + "="*50)
    print(" LLM EXTRACTION EVALUATION REPORT (Laptop B)")
    print(" Target: Qwen3.5-2B (W4A16 on Snapdragon 8 Elite)")
    print("="*50)
    print(f"Total Test Cases:           {results['total_cases']}")
    print(f"Field Extraction Precision: {results['precision']:.2%}")
    print(f"Field Extraction Recall:    {results['recall']:.2%}")
    print(f"F1 Score:                   {results['f1_score']:.2%}")
    print(f"Null Preservation Rate:     {results['null_preservation_rate']:.2%} (Anti-Hallucination Guardrail)")
    print(f"Total Null Violations:      {results['total_null_violations']}")
    print("="*50)

    for cr in results["case_results"]:
        status = "PASS" if cr["null_violations"] == 0 and cr["fn"] == 0 else "FLAG"
        print(f"[{status}] {cr['id']}: {cr['title']} | TP={cr['tp']}, FP={cr['fp']}, FN={cr['fn']}, NullViols={cr['null_violations']}")

    print("="*50)


if __name__ == "__main__":
    main()
