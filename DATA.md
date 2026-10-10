# Data Contract and Report Schema

## Purpose

The schema is the contract between extraction, deterministic workflow logic, UI, evaluation, and final submission. Agree on the exact required fields with the organizers/team before treating any field as mandatory.

## Proposed report schema

This is a **provisional application schema** based on the fields shown in the problem statement. It is not confirmed as the organizer's exact API/schema.

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

Suggested types:
- Scalar descriptive fields: `string | null`
- `persons_involved`: `array[string] | null` (or a structured object if the team agrees)
- `witness_information`: `array[string] | null` (or a structured object if the team agrees)

Freeze the types before implementation and keep them identical in prompts, validation, UI, and evaluation labels.

## Null and evidence policy

- Unknown or unstated values remain `null`.
- Do not infer severity from incident type unless the product explicitly defines a deterministic rule and clearly labels the result as inferred.
- Do not invent names, witnesses, assets, dates, or actions.
- Normalization is allowed when meaning is preserved (for example, converting an explicitly stated time into a consistent representation).
- Preserve the original transcript for audit/debugging when appropriate and safe.
- Every populated field should carry provenance in application state.

## Provenance proposal

Provenance can be stored separately from the user-facing schema:

```json
{
  "incident_location": {
    "value": "Conference Room 2",
    "source": "stated",
    "evidence": "near Conference Room 2"
  },
  "severity_level": {
    "value": null,
    "source": "unknown",
    "evidence": null
  }
}
```

Allowed source labels:
- `stated`: directly supported by narration.
- `normalized`: deterministic formatting of a stated value.
- `inferred`: derived from context; must be explicitly labeled and should be minimized.
- `unknown`: no supported value; field remains null.

This provenance envelope is an internal design suggestion; it need not change the final report schema if the organizers require a strict format.

## Extraction output

Prefer a patch/delta so later narration updates the current report without replacing unrelated values:

```json
{
  "updates": {
    "incident_type": "Water leak",
    "incident_location": "Conference Room 2",
    "date_time": "10:00",
    "immediate_actions_taken": "Maintenance isolated the area"
  },
  "evidence": {
    "incident_type": "water leak",
    "incident_location": "near Conference Room 2",
    "date_time": "around 10 AM",
    "immediate_actions_taken": "Maintenance has already isolated the area"
  },
  "unknown_fields": [
    "severity_level",
    "persons_involved",
    "equipment_asset_involved",
    "witness_information"
  ]
}
```

This example illustrates the contract only; choose a date/time representation and exact output structure before implementation.

## Merge rules

1. Parse and validate the patch.
2. Ignore or reject unknown field names.
3. Apply only supported updates.
4. Do not overwrite a populated field with `null` unless the user explicitly clears it.
5. If the user corrects a field, replace that field and retain unrelated fields.
6. Recompute missing mandatory fields after every merge.
7. Keep provenance/evidence aligned with the current value.
