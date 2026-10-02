---
name: fix-and-prove
description: Reproduce a defect with a failing test, fix the root cause, and prove it with a passing run
agent: agent
argument-hint: "Describe the defect and which backend (node|java)"
---

Defect: ${input:defect:Describe the defect and the expected behaviour}
Backend: ${input:stack:node or java}

Follow this loop and don't skip steps:

1. **Reproduce:** write the smallest failing test that shows the defect, using the test conventions for this backend. Name it after the expected behaviour.
2. **Prove it fails:** run the backend's tests and confirm the new test fails *for the reason described*, not because of a typo or setup problem. Show me the failure.
3. **Fix the root cause:** change only what's needed. Don't add catch-alls or defaults that hide the problem. Behaviour must match [docs/API.md](../../docs/API.md).
4. **Prove it's fixed:** run the full test suite for this backend and confirm everything passes.
5. **Summarize** in 3 bullets: root cause (one sentence), the fix, and whether the other backend has the same defect.
