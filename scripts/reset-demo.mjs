#!/usr/bin/env node
// Restores the demo application to the permanent known-good baseline.

import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const baselineTag = 'demo-base-2026-10-02';
const trackedPaths = ['client', 'server-node', 'server-java', 'specs', 'docs/API.md'];
const cleanPaths = ['client', 'server-node', 'server-java', 'specs'];

function runGit(args, stdio = 'inherit', failureMessage) {
  const result = spawnSync('git', args, { cwd: root, encoding: 'utf8', stdio });
  if (result.error) {
    console.error(`Failed to run git: ${result.error.message}`);
    process.exit(1);
  }
  if (result.status !== 0) {
    if (failureMessage) console.error(failureMessage);
    if (stdio === 'pipe' && result.stderr) process.stderr.write(result.stderr);
    process.exit(result.status ?? 1);
  }
}

runGit(
  ['rev-parse', '--verify', '--quiet', `refs/tags/${baselineTag}^{commit}`],
  'pipe',
  `Required baseline tag ${baselineTag} was not found. Fetch tags from origin and try again.`,
);
runGit(['restore', '--source', baselineTag, '--staged', '--worktree', '--', ...trackedPaths]);
runGit(['clean', '-fd', '--', ...cleanPaths]);

console.log(`Demo restored to ${baselineTag}. Restart the API and client.`);
