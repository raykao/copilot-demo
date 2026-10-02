---
name: rca
description: Root cause analysis from a symptom and evidence. Explains first, doesn't fix.
agent: ask
argument-hint: "Describe the symptom you saw"
---

You are doing a root cause analysis. **Don't propose or apply a fix yet.**

**Symptom:** ${input:symptom:What did you observe? e.g. "Checkout with a Gift Card returns 500"}

**Evidence:** use whatever I've attached (log excerpt, `#terminalLastCommand`, `#file`). If a request ID is mentioned, find every line for that ID in `logs/app.log`. Also use [docs/API.md](../../docs/API.md) as the definition of correct behaviour.

Work through it in this order:
1. **Classify** the defect: build/compile error, runtime exception, or silent logic defect.
2. **Trace** the path from the entry point (route or controller) to the failing line, citing file and line numbers.
3. **Root cause:** name the exact state or assumption that's wrong, and whether the real problem is upstream (data, contract, or a missing validation) rather than at the line that failed.
4. **Blast radius:** list every other code path or input that could hit the same defect, including in the *other* backend.
5. **Fix options:** give 2–3 options with trade-offs, and recommend one that matches the API contract.

Keep it concise and evidence-based. If you're guessing, say so.
