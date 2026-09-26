import { count } from "drizzle-orm";
import type { RoadmapDb } from "./database";
import type { Seed } from "./seed-types";
import {
  changeLog,
  decisions,
  domains,
  openQuestions,
  phaseDependencies,
  phases,
  systems,
  tasks,
} from "./schema";

/**
 * Imports `seed` into the database if it holds no systems yet. The import runs in
 * one transaction and records a `seed` entry in the change log.
 *
 * @param db the database to import into
 * @param seed the seed content
 * @return `true` if the seed was imported, `false` if the database already had systems
 */
export function importSeed(db: RoadmapDb, seed: Seed): boolean {
  const [{ value: existing }] = db.select({ value: count() }).from(systems).all();
  if (existing > 0) return false;

  db.transaction((tx) => {
    seed.domains.forEach((d, i) => {
      tx.insert(domains).values({ id: d.id, name: d.name, description: d.description, sortOrder: i }).run();
    });
    seed.phases.forEach((p, i) => {
      tx.insert(phases).values({ id: p.id, name: p.name, goal: p.goal, sortOrder: i }).run();
    });
    for (const p of seed.phases) {
      for (const dep of p.dependsOn) {
        tx.insert(phaseDependencies).values({ phaseId: p.id, dependsOnId: dep }).run();
      }
    }
    seed.systems.forEach((s, i) => {
      tx.insert(systems)
        .values({
          id: s.id,
          domainId: s.domain,
          phaseId: s.phase,
          title: s.title,
          summary: s.summary,
          spec: s.spec,
          status: s.status ?? "Not started",
          priority: s.priority,
          ownerId: null,
          notes: "",
          sortOrder: i,
        })
        .run();
      s.tasks.forEach((title, j) => {
        tx.insert(tasks)
          .values({ systemId: s.id, title, status: "Not started", priority: s.priority, ownerId: null, sortOrder: j })
          .run();
      });
    });
    for (const d of seed.decisions) {
      tx.insert(decisions).values({ id: d.id, title: d.title, text: d.text, adr: d.adr ?? null, date: d.date }).run();
    }
    for (const q of seed.questions) {
      tx.insert(openQuestions)
        .values({ id: q.id, title: q.title, text: q.text, systemId: q.system ?? null, resolved: false })
        .run();
    }
    tx.insert(changeLog)
      .values({
        entity: "seed",
        entityId: "roadmap",
        field: "version",
        oldValue: null,
        newValue: String(seed.version),
        author: "seed",
        createdAt: new Date().toISOString(),
      })
      .run();
  });
  return true;
}
