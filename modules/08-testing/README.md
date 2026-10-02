# Module 8: Unit testing with TDD, BDD and spec-driven development

**Time:** about 14 minutes · **Goal:** use Copilot to write tests *first* and let them drive the implementation. There are four exercises. Pick the ones that fit your time and audience, and save the rest for a follow-up session.

| Exercise | Time | Spec | Prompt |
|---|---|---|---|
| A. TDD: loyalty calculator v2 | 5 min | [specs/loyalty-points/REQUIREMENTS.md](../../specs/loyalty-points/REQUIREMENTS.md) | `/tdd-next` |
| B. BDD: discount eligibility | 4 min | [specs/discount-eligibility/discount-eligibility.feature](../../specs/discount-eligibility/discount-eligibility.feature) | `/bdd-steps` |
| C. SDD: password reset tokens | 4 min (plan) or 8 min (plan + build) | [specs/password-reset/spec.md](../../specs/password-reset/spec.md) | `/sdd-plan` |
| D. Documentation | 1–2 min | Any file you touched | `/doc` |

Exercise A builds on the loyalty feature from Module 6. If you skipped it, run the **Demo: jump to stage** task and choose `01-scaffold` first.

## A. Test-driven development (red → green → refactor)

The calculator from Module 6 only implements requirement 1. In **Agent** mode:

```text
/tdd-next requirements=specs/loyalty-points/REQUIREMENTS.md stack=node
```

Copilot finds the first untested requirement, writes **one** failing test, shows it failing, writes the minimum code to pass, and stops. Run it again for the next requirement. Two or three loops make the point.

Talking point: the prompt deliberately stops after each step. TDD with an AI only works if you keep the loop small.

## B. Behavior-driven development (Gherkin → steps → code)

```text
/bdd-steps feature=specs/discount-eligibility/discount-eligibility.feature stack=java
```

Watch for:
- Step definitions are generated **before** the implementation, and the first run fails.
- The Background table becomes data, not hard-coded `if` statements.
- The Scenario Outline boundaries (`9` vs `10` items, `499.99` vs `500.00`) drive the `>=` comparisons.

Both stacks read the same `.feature` file from `specs/`. That's one source of truth for business behavior across two implementations.

## C. Spec-driven development (spec → plan → code)

```text
/sdd-plan spec=specs/password-reset/spec.md stack=node
```

Copilot writes `specs/password-reset/plan.md` and **stops for review**. Look at the plan's traceability table: every FR, NFR and acceptance criterion maps to a test. Ask questions like these:

- "How are tokens stored? Can a leaked store be replayed?" (A good plan hashes them.)
- "How do the tests control time and randomness?"

Approve it to have Copilot implement tests first. If you're short on time, stopping at the plan still shows the method.

## D. Documentation generation

Select a class or function you touched today (for example `LoyaltyService`) and run:

```text
/doc
```

Then ask: *"Does this documentation match what the tests prove?"*

## If something goes wrong

Run the **Demo: jump to stage** task and choose `03-tests` to see completed versions of all four exercises in both stacks.
