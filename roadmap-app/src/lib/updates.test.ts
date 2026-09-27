import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { openDatabase, type OpenDatabase } from "@/db/database";
import { importSeed } from "@/db/seed";
import type { Seed } from "@/db/seed-types";
import { addQuestion, agentAuthor, postUpdate } from "./mutations";

const SEED: Seed = {
  version: 1,
  domains: [{ id: "d", name: "D", description: "" }],
  phases: [{ id: "p0", name: "P0", goal: "", dependsOn: [] }],
  systems: [
    { id: "s1", domain: "d", phase: "p0", title: "S1", summary: "", spec: "", priority: "MVP", tasks: ["t1"] },
    { id: "s2", domain: "d", phase: "p0", title: "S2", summary: "", spec: "", priority: "MVP", tasks: ["t2"] },
  ],
  decisions: [],
  questions: [],
};

let dir: string;
let o: OpenDatabase;

beforeEach(() => {
  dir = mkdtempSync(join(tmpdir(), "roadmap-upd-"));
  o = openDatabase(join(dir, "roadmap.db"));
  importSeed(o.db, SEED);
});

afterEach(() => {
  o.sqlite.close();
  rmSync(dir, { recursive: true, force: true });
});

/** Returns the id of the first task of `systemId`. */
function taskOf(systemId: string): number {
  return (o.sqlite.prepare("SELECT id FROM tasks WHERE system_id = ?").get(systemId) as { id: number }).id;
}

describe("agentAuthor", () => {
  it("formats agent and person", () => {
    expect(agentAuthor("Claude", "Ammo")).toBe("Claude (for Ammo)");
  });

  it("rejects a missing agent or person", () => {
    expect(() => agentAuthor(" ", "Ammo")).toThrow();
    expect(() => agentAuthor("Claude", "")).toThrow();
  });
});

describe("postUpdate", () => {
  it("stores an update with all fields and logs it", () => {
    const id = postUpdate(
      o.db,
      { systemId: "s1", taskId: taskOf("s1"), summary: "Did it", nextStep: "More", commit: "abc1234" },
      { author: "Claude (for Ammo)", agent: true },
    );
    const row = o.sqlite.prepare("SELECT * FROM progress_updates WHERE id = ?").get(id) as Record<string, unknown>;
    expect(row).toMatchObject({
      system_id: "s1",
      summary: "Did it",
      next_step: "More",
      commit_hash: "abc1234",
      author: "Claude (for Ammo)",
      agent: 1,
    });
    const log = o.sqlite.prepare("SELECT entity, field, new_value FROM change_log WHERE entity = 'update'").get();
    expect(log).toEqual({ entity: "update", field: "posted", new_value: "Did it" });
  });

  it("rejects an empty summary", () => {
    expect(() => postUpdate(o.db, { systemId: "s1", summary: "  " }, { author: "a", agent: true })).toThrow();
  });

  it("rejects an unknown system", () => {
    expect(() => postUpdate(o.db, { systemId: "nope", summary: "x" }, { author: "a", agent: true })).toThrow();
  });

  it("rejects a task of another system", () => {
    expect(() =>
      postUpdate(o.db, { systemId: "s1", taskId: taskOf("s2"), summary: "x" }, { author: "a", agent: true }),
    ).toThrow();
  });

  it("rejects a commit hash that is not 7 to 40 hex characters", () => {
    for (const commit of ["abc12", "zzzzzzz", "a".repeat(41)]) {
      expect(() => postUpdate(o.db, { systemId: "s1", summary: "x", commit }, { author: "a", agent: true })).toThrow();
    }
  });

  it("stores the commit hash in lowercase", () => {
    const id = postUpdate(o.db, { systemId: "s1", summary: "x", commit: "ABC1234" }, { author: "a", agent: false });
    expect(o.sqlite.prepare("SELECT commit_hash FROM progress_updates WHERE id = ?").get(id)).toEqual({
      commit_hash: "abc1234",
    });
  });
});

describe("addQuestion", () => {
  it("creates an unresolved question linked to a system and logs it", () => {
    const id = addQuestion(o.db, { title: "Why?", text: "Because", systemId: "s1" }, "Claude (for Ammo)");
    expect(o.sqlite.prepare("SELECT title, text, system_id, resolved FROM open_questions WHERE id = ?").get(id)).toEqual({
      title: "Why?",
      text: "Because",
      system_id: "s1",
      resolved: 0,
    });
    expect(o.sqlite.prepare("SELECT field FROM change_log WHERE entity = 'question'").get()).toEqual({ field: "created" });
  });

  it("rejects an empty title", () => {
    expect(() => addQuestion(o.db, { title: " ", text: "" }, "a")).toThrow();
  });
});
