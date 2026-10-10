# Evaluation Plan

## Dataset
Start with ~40 local narrations. Each case has an ID, audio fixture/instructions, reference transcript, ground-truth fields, expected missing fields and category tags. Do not upload audio to external services.

## Metrics
- WER = (substitutions + deletions + insertions) / reference word count.
- Field-level exact match, with documented normalization for dates/times.
- Precision/recall/F1 for list fields where applicable.
- Null correctness and unsupported-field/hallucination rate.
- Schema-valid rate, follow-up completion and correction success.
- Per-stage and end-to-end latency.
- Airplane-mode pass/fail and evidence of Snapdragon acceleration.

## Cases
Clean speech; conversational speech; available accent/language variation; moderate noise; missing required fields; multi-turn additions; corrections; contradictory statements; empty/unintelligible audio; malformed JSON/runtime failure; airplane mode; unstated values that must remain null.

## Internal acceptance targets
These are team targets, not official judging thresholds:
- No crashes on empty audio, invalid output or timeout.
- Zero silently invented fields in the labeled test set.
- Full core flow passes in airplane mode.
- Publish reproducible WER, field accuracy and latency breakdowns.
