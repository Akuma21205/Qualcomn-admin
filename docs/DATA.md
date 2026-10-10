# Data Contract

## Provisional report schema
Confirm mandatory fields and exact types with organizers before freezing.

```json
{
  "incident_type": null,
  "severity_level": null,
  "incident_location": null,
  "date_time": null,
  "persons_involved": null,
  "equipment_asset_involved": null,
  "incident_description": null,
  "immediate_actions_taken": null,
  "witness_information": null,
  "recommended_follow_up_action": null
}
```

Proposed Kotlin types: descriptive fields `String?`; people/witness fields `List<String>?`.

## Rules
- Unknown or unstated values remain null.
- Never invent severity, names, witnesses, assets, actions or dates.
- Normalize only when meaning is preserved.
- Track provenance internally: stated, normalized, inferred, unknown.
- Model output is untrusted until parsed and validated.
- Use a presence-aware patch: omitted field = no update; explicit clear = clear; value = update. Nullable properties alone do not encode this distinction.

## Merge rules
1. Reject unknown keys and wrong types.
2. Apply only fields explicitly present in a validated patch.
3. Do not erase existing values when a field is omitted.
4. User corrections replace only the targeted field.
5. Keep provenance/evidence aligned with each value.
6. Recompute missing mandatory fields after each update.
