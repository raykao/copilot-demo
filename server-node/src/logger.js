import fs from 'node:fs';
import path from 'node:path';
import { AsyncLocalStorage } from 'node:async_hooks';

/** Carries the current request ID so every log line can be correlated. */
export const requestContext = new AsyncLocalStorage();

const formatValue = (value) => (typeof value === 'string' ? value : JSON.stringify(value));

const formatFields = (fields = {}) =>
  Object.entries(fields)
    .map(([key, value]) => ` ${key}=${formatValue(value)}`)
    .join('');

/**
 * Creates a sink that writes each log line to stdout and, optionally, appends it to a file.
 * @param {{ file?: string, console?: boolean }} options
 * @returns {(line: string) => void}
 */
export function createSink({ file, console: toConsole = true } = {}) {
  let stream = null;
  if (file) {
    fs.mkdirSync(path.dirname(file), { recursive: true });
    stream = fs.createWriteStream(file, { flags: 'a' });
  }
  return (line) => {
    if (toConsole) process.stdout.write(`${line}\n`);
    stream?.write(`${line}\n`);
  };
}

/**
 * Plain-text logger whose format matches the Java backend:
 * `<timestamp> <LEVEL> [<requestId>] <logger> - <message> key=value ...`
 */
export class Logger {
  #name;
  #sink;

  constructor(name, sink) {
    this.#name = name;
    this.#sink = sink;
  }

  child(name) {
    return new Logger(name, this.#sink);
  }

  debug(message, fields) {
    this.#write('DEBUG', message, fields);
  }

  info(message, fields) {
    this.#write('INFO', message, fields);
  }

  warn(message, fields) {
    this.#write('WARN', message, fields);
  }

  error(message, fields, err) {
    this.#write('ERROR', message, fields, err);
  }

  #write(level, message, fields, err) {
    const requestId = requestContext.getStore()?.requestId ?? '-';
    let line = `${new Date().toISOString()} ${level.padEnd(5)} [${requestId}] ${this.#name} - ${message}${formatFields(fields)}`;
    if (err?.stack) line += `\n${err.stack}`;
    this.#sink(line);
  }
}

export const silentLogger = new Logger('silent', () => {});
