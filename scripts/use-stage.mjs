#!/usr/bin/env node
// Recovery helper: replaces the working app with a known-good snapshot from solutions/.
// Usage: node scripts/use-stage.mjs <01-scaffold|02-debug|03-tests>
// To get back to the starting state instead, use git: git checkout -- client server-node server-java specs docs/API.md

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const solutionsDir = path.join(root, 'solutions');
const parts = ['client', 'server-node', 'server-java', 'specs'];
const skip = new Set(['node_modules', 'target', 'dist']);

const stages = fs
  .readdirSync(solutionsDir, { withFileTypes: true })
  .filter((d) => d.isDirectory())
  .map((d) => d.name);

const stage = process.argv[2];
if (!stages.includes(stage)) {
  console.error(`Usage: node scripts/use-stage.mjs <${stages.join('|')}>`);
  process.exit(1);
}

for (const part of parts) {
  const from = path.join(solutionsDir, stage, part);
  if (!fs.existsSync(from)) continue;
  const to = path.join(root, part);

  // Remove tracked sources but keep installed dependencies and build output.
  for (const entry of fs.readdirSync(to)) {
    if (!skip.has(entry)) fs.rmSync(path.join(to, entry), { recursive: true, force: true });
  }
  fs.cpSync(from, to, { recursive: true, filter: (src) => !skip.has(path.basename(src)) });
  console.log(`  ${part} <- solutions/${stage}/${part}`);
}

const contract = path.join(solutionsDir, stage, 'docs/API.md');
if (fs.existsSync(contract)) {
  fs.copyFileSync(contract, path.join(root, 'docs/API.md'));
  console.log(`  docs/API.md <- solutions/${stage}/docs/API.md`);
}

console.log(`\nNow at stage ${stage}. Restart the API so it picks up the changes.`);
