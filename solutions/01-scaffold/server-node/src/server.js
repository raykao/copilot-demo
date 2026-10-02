import { config } from './config.js';
import { Logger, createSink } from './logger.js';
import { Store, loadSeed } from './store.js';
import { createApp } from './app.js';

const logger = new Logger('server', createSink({ file: config.logFile }));
const store = new Store(loadSeed(config.seedPath));
const app = createApp({ store, logger, taxRate: config.taxRate });

app.listen(config.port, () => {
  logger.info('storefront API listening', { backend: 'node', port: config.port, logFile: config.logFile });
});
