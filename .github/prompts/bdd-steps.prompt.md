---
name: bdd-steps
description: Turn a Gherkin feature file into step definitions, then the implementation
agent: agent
argument-hint: "feature file + backend"
---

Feature file: `${input:feature:specs/discount-eligibility/discount-eligibility.feature}`
Backend: `${input:stack:node or java}`

1. Read the feature file. List the distinct step phrases, including the Background and Scenario Outline steps.
2. Generate step definitions **before** any implementation:
   - Node: a `jest-cucumber` test in `server-node/test/` that loads the feature from `../specs` (use `autoBindSteps` so Background steps are shared).
   - Java: Cucumber step definitions under `server-java/src/test/java/.../<feature>/`, plus a JUnit Platform `@Suite` that selects the feature directory under `../specs`.
3. Run the tests and show me they fail because the implementation doesn't exist yet.
4. Implement the smallest production code that satisfies every scenario, following the backend's conventions. Use the tiers from the Background table as data; don't hard-code them.
5. Run the tests until everything passes, then summarize which scenario drove which rule.
