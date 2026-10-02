import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');

export const config = Object.freeze({
  port: Number(process.env.PORT ?? 3001),
  seedPath: process.env.SEED_PATH ?? path.join(repoRoot, 'data/seed.json'),
  logFile: process.env.LOG_FILE ?? path.join(repoRoot, 'logs/app.log'),
  taxRate: Number(process.env.TAX_RATE ?? 0.08),
});
