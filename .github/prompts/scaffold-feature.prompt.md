---
name: scaffold-feature
description: Scaffold a full-stack feature (service, routes, client UI, tests) from a spec, matching existing patterns
agent: agent
argument-hint: "spec=specs/<feature>/FEATURE.md stack=node|java|both"
---

Scaffold the feature described in `${input:spec:specs/loyalty-points/FEATURE.md}` for backend `${input:stack:node, java, or both}`.

Before you write any code:
1. Read the spec, [docs/API.md](../../docs/API.md) and [copilot-instructions](../copilot-instructions.md).
2. Find the closest existing feature in the chosen backend (for example pricing or orders). List the files you'll mirror and the conventions you'll follow: error classes, logger usage, dependency injection, and test style.
3. Show me a short file plan (create/modify, one line each) and wait for my OK.

When I approve the plan:
- Implement the backend layers (service → route/controller → wiring) for the chosen backend.
- Add the endpoint(s) to `client/src/api/client.js` and build the UI component described in the spec, then wire it into the existing screens.
- Add the new endpoint(s) to `docs/API.md`.
- Add a happy-path HTTP test and one validation/error test.
- Run the backend's test command and fix any failures.
- Finish with a summary of what you created and how to try it in the browser.

Don't implement anything the spec lists as out of scope.
