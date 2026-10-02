#!/usr/bin/env node
// Restores any files changed by scripts/break-build.mjs.

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const backupDir = path.join(root, '.demo-backup');

if (!fs.existsSync(backupDir)) {
  console.log('Nothing to restore.');
  process.exit(0);
}

const restored = [];
for (const entry of fs.readdirSync(backupDir, { recursive: true, withFileTypes: true })) {
  if (!entry.isFile()) continue;
  const backup = path.join(entry.parentPath ?? entry.path, entry.name);
  const relative = path.relative(backupDir, backup);
  fs.copyFileSync(backup, path.join(root, relative));
  restored.push(relative);
}
fs.rmSync(backupDir, { recursive: true, force: true });

console.log(restored.length ? `Restored:\n  ${restored.join('\n  ')}` : 'Nothing to restore.');
