#!/usr/bin/env python3
"""
Qualcomm Incident Report Assistant - Speech-to-Text WER Evaluation Benchmark
Computes Word Error Rate (WER) using Levenshtein distance on tokenized word sequences.
Used to compare Whisper-Small vs Whisper-Base transcription accuracy.
"""

import sys
import argparse
from typing import List, Tuple


def calculate_wer(reference: str, hypothesis: str) -> Tuple[float, int, int, int, int]:
    """
    Computes WER and edit distances (substitutions, deletions, insertions).
    Returns (wer, substitutions, deletions, insertions, total_words).
    """
    ref_words = reference.strip().lower().split()
    hyp_words = hypothesis.strip().lower().split()

    n = len(ref_words)
    m = len(hyp_words)

    if n == 0:
        return (1.0 if m > 0 else 0.0), 0, 0, m, 0

    # DP table for edit distance
    dp = [[0] * (m + 1) for _ in range(n + 1)]

    for i in range(n + 1):
        dp[i][0] = i
    for j in range(m + 1):
        dp[0][j] = j

    for i in range(1, n + 1):
        for j in range(1, m + 1):
            if ref_words[i - 1] == hyp_words[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                sub = dp[i - 1][j - 1] + 1
                ins = dp[i][j - 1] + 1
                delete = dp[i - 1][j] + 1
                dp[i][j] = min(sub, ins, delete)

    distance = dp[n][m]
    wer = distance / n
    return wer, distance, 0, 0, n


def main():
    parser = argparse.ArgumentParser(description="Evaluate Word Error Rate (WER).")
    parser.add_argument("--ref", help="Reference transcript string", default="emergency in processing plant 4 bay 2")
    parser.add_argument("--hyp", help="Hypothesis transcript string", default="emergency in processing plant four bay 2")

    args = parser.parse_args()

    wer, edits, _, _, words = calculate_wer(args.ref, args.hyp)
    print("=" * 50)
    print(" STT WORD ERROR RATE (WER) EVALUATION")
    print("=" * 50)
    print(f"Reference:  {args.ref}")
    print(f"Hypothesis: {args.hyp}")
    print(f"Word Count: {words}")
    print(f"Edits:      {edits}")
    print(f"WER:        {wer:.2%}")
    print("=" * 50)


if __name__ == "__main__":
    main()
