# Evaluation Plan

## Goal

Demonstrate that the assistant transcribes natural speech and maps it accurately into the incident schema while remaining fully offline.

## Test set

Start with approximately 40 scripted narrations, as proposed in the handoff. Store each case with:
- Stable case ID.
- Audio file or repeatable recording instructions.
- Reference transcript.
- Ground-truth field values.
- Required fields expected to be missing.
- Test category (clean speech, accent, noise, correction, multi-turn, etc.).

Keep development/evaluation data local. Do not upload incident audio to external services.

## Metrics

### Word Error Rate (WER)

\[
WER = \frac{S + D + I}{N}
\]

Where `S` is substitutions, `D` deletions, `I` insertions, and `N` the number of words in the reference transcript. Document text normalization rules (case, punctuation, numbers) so scores are comparable.

### Field-level extraction metrics

For each field, report:
- Exact-match accuracy for categorical/simple string fields.
- Normalized exact match for values such as time, where a documented normalization is used.
- Precision/recall/F1 for multi-value fields such as persons involved, if implemented as lists.
- Null/abstention correctness: whether the model leaves unsupported values empty.
- Hallucination rate: populated values without supporting evidence.

Also report:
- Overall field accuracy across all labeled fields.
- Mandatory-field completion rate.
- JSON/schema validity rate.
- Correction success rate.
- Follow-up question count and completion rate.
- End-to-end latency and per-stage latency.
- Offline test pass/fail.

## Test categories

1. Clean, single-sentence narration.
2. Natural conversational narration with extra irrelevant words.
3. Different speakers/accents available to the team.
4. Moderate background noise.
5. Missing mandatory field that should trigger a follow-up.
6. Multi-turn addition of a person, witness, asset, or action.
7. Explicit correction (e.g. “actually make that 11 AM”).
8. Contradictory narration requiring clarification.
9. No speech / unintelligible speech.
10. Malformed model output or runtime error.
11. Offline/airplane-mode end-to-end run.
12. Unknown fields that must remain null.

## Suggested acceptance gates

These are proposed internal targets, **not organizer-provided thresholds**. Adjust after measuring the baseline and confirm judging expectations with mentors.

- 100% of test cases produce schema-valid app state (including safe failure states).
- 0 unsupported fields silently filled with invented values in the labeled test set.
- 100% pass for airplane-mode core workflow.
- Field accuracy and WER reported with per-category breakdowns.
- No crash on empty audio, malformed JSON, or inference timeout.

Do not claim accuracy targets until the evaluation harness has actually run.

## Reproducibility

Record device build, model/runtime versions, app commit, prompt version, test-set version, and whether acceleration was active. Keep a CSV/JSON result artifact and a concise summary table.
