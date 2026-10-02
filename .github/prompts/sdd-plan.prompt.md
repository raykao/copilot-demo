---
name: sdd-plan
description: Spec-driven development. Turn a spec into an implementation plan before any code is written.
agent: agent
argument-hint: "spec file + backend"
---

Spec: `${input:spec:specs/password-reset/spec.md}`
Backend: `${input:stack:node or java}`

**Step 1: plan only.** Write `plan.md` next to the spec with:
- The public API (function or method signatures and return types)
- The storage interface and its in-memory implementation (NFR3)
- How randomness and time are injected so tests stay deterministic
- A table mapping every FR, NFR and acceptance criterion to the test(s) that will prove it
- Explicit non-goals, taken from the spec's "Out of Scope"

Then **stop** and ask me to review the plan. Don't write code yet.

**Step 2 (only after I approve):** implement the plan, writing tests first. Run the tests until they pass, then check that every row of the traceability table has a passing test.
