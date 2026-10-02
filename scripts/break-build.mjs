#!/usr/bin/env node
// Module 7 demo helper: simulates a half-finished refactor that breaks the build.
// Usage: node scripts/break-build.mjs <node|java>     Undo: node scripts/restore-build.mjs

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const backupDir = path.join(root, '.demo-backup');

const breakages = {
  node: {
    file: 'server-node/src/services/pricingService.js',
    find: 'export class PricingService {',
    replace: 'export class CartPricingService {',
    story: 'PricingService was renamed to CartPricingService, but its importers were not updated.',
    verify: 'cd server-node && npm start',
  },
  java: {
    file: 'server-java/src/main/java/com/contoso/storefront/pricing/PricingService.java',
    find: 'public Quote quote(QuoteRequest request) {',
    replace: 'public Quote calculateQuote(QuoteRequest request) {',
    story: 'PricingService.quote() was renamed to calculateQuote(), but its callers were not updated.',
    verify: 'cd server-java && mvn -q compile',
  },
};

const stack = process.argv[2];
const breakage = breakages[stack];
if (!breakage) {
  console.error('Usage: node scripts/break-build.mjs <node|java>');
  process.exit(1);
}

const target = path.join(root, breakage.file);
const source = fs.readFileSync(target, 'utf8');
if (!source.includes(breakage.find)) {
  console.error(`Nothing to break: "${breakage.find}" not found in ${breakage.file} (already broken?)`);
  process.exit(1);
}

const backup = path.join(backupDir, breakage.file);
fs.mkdirSync(path.dirname(backup), { recursive: true });
fs.copyFileSync(target, backup);
fs.writeFileSync(target, source.replace(breakage.find, breakage.replace));

console.log(`Build broken for ${stack}: ${breakage.story}`);
console.log(`Reproduce it in a terminal:  ${breakage.verify}`);
