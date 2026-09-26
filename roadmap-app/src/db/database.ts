import Database from "better-sqlite3";
import { drizzle, type BetterSQLite3Database } from "drizzle-orm/better-sqlite3";
import { mkdirSync } from "node:fs";
import { dirname } from "node:path";
import * as schema from "./schema";

/** A Drizzle database bound to the roadmap schema. */
export type RoadmapDb = BetterSQLite3Database<typeof schema>;

/** An open database: the Drizzle handle and the underlying SQLite connection. */
export interface OpenDatabase {
  db: RoadmapDb;
  sqlite: Database.Database;
}

/** DDL creating every table if it does not exist yet. Must mirror `schema.ts`. */
const CREATE_TABLES = `
CREATE TABLE IF NOT EXISTS people (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  role TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS domains (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  sort_order INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS phases (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  goal TEXT NOT NULL,
  sort_order INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS phase_dependencies (
  phase_id TEXT NOT NULL REFERENCES phases(id),
  depends_on_id TEXT NOT NULL REFERENCES phases(id),
  PRIMARY KEY (phase_id, depends_on_id)
);
CREATE TABLE IF NOT EXISTS systems (
  id TEXT PRIMARY KEY,
  domain_id TEXT NOT NULL REFERENCES domains(id),
  phase_id TEXT NOT NULL REFERENCES phases(id),
  title TEXT NOT NULL,
  summary TEXT NOT NULL,
  spec TEXT NOT NULL,
  status TEXT NOT NULL,
  priority TEXT NOT NULL,
  owner_id INTEGER REFERENCES people(id) ON DELETE SET NULL,
  notes TEXT NOT NULL,
  sort_order INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS tasks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  system_id TEXT NOT NULL REFERENCES systems(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  status TEXT NOT NULL,
  priority TEXT NOT NULL,
  owner_id INTEGER REFERENCES people(id) ON DELETE SET NULL,
  sort_order INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS decisions (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  text TEXT NOT NULL,
  adr TEXT,
  date TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS open_questions (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  text TEXT NOT NULL,
  system_id TEXT REFERENCES systems(id) ON DELETE SET NULL,
  resolved INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS change_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  entity TEXT NOT NULL,
  entity_id TEXT NOT NULL,
  field TEXT NOT NULL,
  old_value TEXT,
  new_value TEXT,
  author TEXT NOT NULL,
  created_at TEXT NOT NULL
);
`;

/**
 * Opens the SQLite database at `path`, creating the file, its directory and every
 * table if they do not exist. WAL mode and foreign keys are enabled.
 *
 * @param path filesystem path of the database file
 * @return the Drizzle handle and the raw connection
 */
export function openDatabase(path: string): OpenDatabase {
  mkdirSync(dirname(path), { recursive: true });
  const sqlite = new Database(path);
  sqlite.pragma("journal_mode = WAL");
  sqlite.pragma("foreign_keys = ON");
  sqlite.exec(CREATE_TABLES);
  return { db: drizzle(sqlite, { schema }), sqlite };
}
