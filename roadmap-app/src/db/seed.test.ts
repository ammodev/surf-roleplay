import { afterEach, describe, expect, it } from "vitest";
import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { openDatabase, type OpenDatabase } from "./database";
import { importSeed } from "./seed";
import type { Seed } from "./seed-types";
import { roadmapSeed } from "../../seed/roadmap";

const FIXTURE: Seed = {
  version: 1,
  domains: [{ id: "d1", name: "Domain", description: "A domain" }],
  phases: [
    { id: "p0", name: "P0", goal: "First", dependsOn: [] },
    { id: "p1", name: "P1", goal: "Second", dependsOn: ["p0"] },
  ],
  systems: [
    { id: "s1", domain: "d1", phase: "p0", title: "S1", summary: "s", spec: "spec", priority: "MVP", tasks: ["t1", "t2"] },
    { id: "s2", domain: "d1", phase: "p1", title: "S2", summary: "s", spec: "spec", priority: "Later", status: "Done", tasks: [] },
  ],
  decisions: [{ id: "dec1", title: "D", text: "t", adr: "ADR-0001", date: "2026-09-26" }],
  questions: [{ id: "q1", title: "Q", text: "t", system: "s1" }],
};

let dir: string | undefined;
let open: OpenDatabase | undefined;

afterEach(() => {
  open?.sqlite.close();
  open = undefined;
  if (dir) rmSync(dir, { recursive: true, force: true });
  dir = undefined;
});

/** Opens a fresh database in a temporary directory. */
function freshDb(): OpenDatabase {
  dir = mkdtempSync(join(tmpdir(), "roadmap-seed-"));
  open = openDatabase(join(dir, "roadmap.db"));
  return open;
}

/** Counts the rows of `table`. */
function count(o: OpenDatabase, table: string): number {
  return (o.sqlite.prepare(`SELECT COUNT(*) AS c FROM ${table}`).get() as { c: number }).c;
}

describe("importSeed", () => {
  it("imports every entity of the seed into an empty database", () => {
    const o = freshDb();
    expect(importSeed(o.db, FIXTURE)).toBe(true);

    expect(count(o, "domains")).toBe(1);
    expect(count(o, "phases")).toBe(2);
    expect(count(o, "phase_dependencies")).toBe(1);
    expect(count(o, "systems")).toBe(2);
    expect(count(o, "tasks")).toBe(2);
    expect(count(o, "decisions")).toBe(1);
    expect(count(o, "open_questions")).toBe(1);
  });

  it("uses Not started unless the seed gives a status", () => {
    const o = freshDb();
    importSeed(o.db, FIXTURE);
    const rows = o.sqlite.prepare("SELECT id, status FROM systems ORDER BY id").all();
    expect(rows).toEqual([
      { id: "s1", status: "Not started" },
      { id: "s2", status: "Done" },
    ]);
  });

  it("records the import in the change log", () => {
    const o = freshDb();
    importSeed(o.db, FIXTURE);
    const row = o.sqlite.prepare("SELECT entity, field, new_value, author FROM change_log").get();
    expect(row).toEqual({ entity: "seed", field: "version", new_value: "1", author: "seed" });
  });

  it("does nothing when systems already exist", () => {
    const o = freshDb();
    importSeed(o.db, FIXTURE);
    expect(importSeed(o.db, FIXTURE)).toBe(false);
    expect(count(o, "systems")).toBe(2);
    expect(count(o, "tasks")).toBe(2);
    expect(count(o, "change_log")).toBe(1);
  });
});

describe("roadmapSeed", () => {
  it("imports completely with matching row counts", () => {
    const o = freshDb();
    importSeed(o.db, roadmapSeed);
    const taskCount = roadmapSeed.systems.reduce((n, s) => n + s.tasks.length, 0);

    expect(count(o, "domains")).toBe(roadmapSeed.domains.length);
    expect(count(o, "phases")).toBe(roadmapSeed.phases.length);
    expect(count(o, "systems")).toBe(roadmapSeed.systems.length);
    expect(count(o, "tasks")).toBe(taskCount);
    expect(count(o, "decisions")).toBe(roadmapSeed.decisions.length);
    expect(count(o, "open_questions")).toBe(roadmapSeed.questions.length);
  });

  it("has unique system ids", () => {
    const ids = roadmapSeed.systems.map((s) => s.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  it("references only existing domains, phases and systems", () => {
    const domains = new Set(roadmapSeed.domains.map((d) => d.id));
    const phases = new Set(roadmapSeed.phases.map((p) => p.id));
    const systems = new Set(roadmapSeed.systems.map((s) => s.id));

    for (const s of roadmapSeed.systems) {
      expect(domains, s.id).toContain(s.domain);
      expect(phases, s.id).toContain(s.phase);
    }
    for (const p of roadmapSeed.phases) for (const d of p.dependsOn) expect(phases, p.id).toContain(d);
    for (const q of roadmapSeed.questions) if (q.system) expect(systems, q.id).toContain(q.system);
  });
});
