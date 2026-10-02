---
name: tdd-next
description: One TDD red-green-refactor step for the next unmet requirement
agent: agent
argument-hint: "requirements file + backend"
---

We're doing strict test-driven development.

Requirements: `${input:requirements:specs/loyalty-points/REQUIREMENTS.md}`
Backend: `${input:stack:node or java}`

1. Read the requirements and the existing tests for this feature. Find the **first requirement that has no passing test yet**, and tell me which one it is.
2. **Red:** write exactly one test for that requirement. Run it and show me it fails.
3. **Green:** write the *minimum* production code to make it pass. Don't implement any later requirement early. Run the tests.
4. **Refactor:** suggest one refactor if it's worth doing (naming, duplication). Apply it only if all tests still pass.
5. Stop and tell me which requirement comes next. Don't continue on your own.
