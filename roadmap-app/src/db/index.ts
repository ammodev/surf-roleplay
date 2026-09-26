import "server-only";
import { openDatabase, type RoadmapDb } from "./database";
import { importSeed } from "./seed";
import { roadmapSeed } from "../../seed/roadmap";

/** Process-wide cache so hot reloads and concurrent requests share one connection. */
const globalForDb = globalThis as unknown as { roadmapDb?: RoadmapDb };

/**
 * Returns the app's database, opening it at `DATABASE_PATH` (default
 * `./data/roadmap.db`) and importing the seed on first start.
 */
export function getDb(): RoadmapDb {
  if (!globalForDb.roadmapDb) {
    const { db } = openDatabase(process.env.DATABASE_PATH ?? "./data/roadmap.db");
    importSeed(db, roadmapSeed);
    globalForDb.roadmapDb = db;
  }
  return globalForDb.roadmapDb;
}
