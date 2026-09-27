import { afterEach, describe, expect, it } from "vitest";
import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { openDatabase } from "./database";

const EXPECTED_TABLES = [
  "change_log",
  "decisions",
  "domains",
  "open_questions",
  "people",
  "phase_dependencies",
  "phases",
  "progress_updates",
  "systems",
  "tasks",
];

let dir: string | undefined;

afterEach(() => {
  if (dir) rmSync(dir, { recursive: true, force: true });
  dir = undefined;
});

/** Creates a fresh temporary directory and returns a database path inside it. */
function tempDbPath(): string {
  dir = mkdtempSync(join(tmpdir(), "roadmap-db-"));
  return join(dir, "nested", "roadmap.db");
}

describe("openDatabase", () => {
  it("creates every table on first open", () => {
    const { sqlite } = openDatabase(tempDbPath());
    const rows = sqlite
      .prepare("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name")
      .all() as { name: string }[];
    sqlite.close();

    expect(rows.map((r) => r.name)).toEqual(EXPECTED_TABLES);
  });

  it("can be opened twice on the same file without error", () => {
    const path = tempDbPath();
    openDatabase(path).sqlite.close();
    const { sqlite } = openDatabase(path);
    sqlite.close();
  });
});
