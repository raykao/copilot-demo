import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { Store, loadSeed } from '../src/store.js';
import { silentLogger } from '../src/logger.js';
import { createApp } from '../src/app.js';

const seedPath = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../data/seed.json');

export const freshStore = () => new Store(loadSeed(seedPath));

export const createTestApp = (overrides = {}) =>
  createApp({ store: freshStore(), logger: silentLogger, taxRate: 0.08, ...overrides });
