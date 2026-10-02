---
name: break-build
description: "Demo helper: simulate a teammate's half-finished refactor that breaks the build"
agent: agent
argument-hint: "node | java"
---

This is a workshop demo helper. Run exactly this command in the terminal and do nothing else:

```
node scripts/break-build.mjs ${input:stack:node or java}
```

Then reply with only: "The build is broken. Run the command printed above in a terminal, then ask Copilot about `#terminalLastCommand`."

Don't inspect, explain or fix the change. The audience will diagnose it in the next step.
