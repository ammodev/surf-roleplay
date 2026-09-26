import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { openDatabase, type OpenDatabase } from "@/db/database";
import { importSeed } from "@/db/seed";
import type { Seed } from "@/db/seed-types";
import {
  addPerson,
  addTask,
  deletePerson,
  deleteTask,
  setQuestionResolved,
  updatePerson,
  updateSystem,
  updateTask,
} from "./mutations";

const SEED: Seed = {
  version: 1,
  domains: [{ id: "d", name: "D", description: "" }],
  phases: [{ id: "p0", name: "P0", goal: "", dependsOn: [] }],
  systems: [{ id: "s1", domain: "d", phase: "p0", title: "S1", summary: "", spec: "", priority: "MVP", tasks: ["t1"] }],
  decisions: [],
  questions: [{ id: "q1", title: "Q", text: "", system: "s1" }],
};

let dir: string;
let o: OpenDatabase;

beforeEach(() => {
  dir = mkdtempSync(join(tmpdir(), "roadmap-mut-"));
  o = openDatabase(join(dir, "roadmap.db"));
  importSeed(o.db, SEED);
});

afterEach(() => {
  o.sqlite.close();
  rmSync(dir, { recursive: true, force: true });
});

/** Returns every change log row except the seed entry, oldest first. */
function log() {
  return o.sqlite
    .prepare("SELECT entity, entity_id, field, old_value, new_value, author FROM change_log WHERE entity != 'seed' ORDER BY id")
    .all();
}

describe("updateSystem", () => {
  it("changes the status and logs old and new value with the author", () => {
    updateSystem(o.db, "s1", { status: "Design" }, "Ann");
    expect(o.sqlite.prepare("SELECT status FROM systems WHERE id = 's1'").get()).toEqual({ status: "Design" });
    expect(log()).toEqual([
      { entity: "system", entity_id: "s1", field: "status", old_value: "Not started", new_value: "Design", author: "Ann" },
    ]);
  });

  it("does not log fields whose value did not change", () => {
    updateSystem(o.db, "s1", { status: "Not started", priority: "Later" }, "Ann");
    expect(log()).toHaveLength(1);
  });

  it("logs owner changes by person name", () => {
    const id = addPerson(o.db, { name: "Bob", role: "dev" }, "Ann");
    updateSystem(o.db, "s1", { ownerId: id }, "Ann");
    expect(log().at(-1)).toMatchObject({ field: "owner", old_value: null, new_value: "Bob" });
  });

  it("rejects an unknown status", () => {
    expect(() => updateSystem(o.db, "s1", { status: "Nope" as never }, "Ann")).toThrow();
  });

  it("attributes changes without a name to unknown", () => {
    updateSystem(o.db, "s1", { notes: "hello" }, "  ");
    expect(log().at(-1)).toMatchObject({ field: "notes", author: "unknown" });
  });
});

describe("tasks", () => {
  it("adds, updates and deletes a task with log entries", () => {
    const id = addTask(o.db, "s1", "t2", "Ann");
    updateTask(o.db, id, { status: "Done" }, "Ann");
    deleteTask(o.db, id, "Ann");

    expect(o.sqlite.prepare("SELECT COUNT(*) AS c FROM tasks").get()).toEqual({ c: 1 });
    expect(log().map((r) => (r as { field: string }).field)).toEqual(["created", "status", "deleted"]);
  });

  it("rejects an empty title", () => {
    expect(() => addTask(o.db, "s1", "   ", "Ann")).toThrow();
  });
});

describe("people", () => {
  it("clears ownership when a person is deleted", () => {
    const id = addPerson(o.db, { name: "Bob", role: "builder" }, "Ann");
    updateSystem(o.db, "s1", { ownerId: id }, "Ann");
    deletePerson(o.db, id, "Ann");
    expect(o.sqlite.prepare("SELECT owner_id FROM systems WHERE id = 's1'").get()).toEqual({ owner_id: null });
  });

  it("renames and changes the role of a person", () => {
    const id = addPerson(o.db, { name: "Bob", role: "builder" }, "Ann");
    updatePerson(o.db, id, { name: "Robert", role: "staff" }, "Ann");
    expect(o.sqlite.prepare("SELECT name, role FROM people").get()).toEqual({ name: "Robert", role: "staff" });
  });
});

describe("setQuestionResolved", () => {
  it("marks a question resolved and logs it", () => {
    setQuestionResolved(o.db, "q1", true, "Ann");
    expect(o.sqlite.prepare("SELECT resolved FROM open_questions").get()).toEqual({ resolved: 1 });
    expect(log().at(-1)).toMatchObject({ entity: "question", field: "resolved", new_value: "true" });
  });
});
