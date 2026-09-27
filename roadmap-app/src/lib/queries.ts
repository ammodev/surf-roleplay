import "server-only";
import { and, asc, count, desc, eq, inArray, or, sql } from "drizzle-orm";
import { getDb } from "@/db";
import {
  changeLog,
  decisions,
  domains,
  openQuestions,
  people,
  phaseDependencies,
  phases,
  progressUpdates,
  systems,
  tasks,
  type Priority,
  type Status,
} from "@/db/schema";

/** A system row enriched with its owner's name and task progress. */
export interface SystemListItem {
  id: string;
  domainId: string;
  phaseId: string;
  title: string;
  summary: string;
  status: Status;
  priority: Priority;
  ownerId: number | null;
  ownerName: string | null;
  tasksTotal: number;
  tasksDone: number;
}

/** Filters accepted by {@link listSystems}; empty values mean "any". */
export interface SystemFilter {
  phase?: string;
  domain?: string;
  status?: string;
  priority?: string;
  owner?: string;
}

/** Returns all domains in display order. */
export function listDomains() {
  return getDb().select().from(domains).orderBy(asc(domains.sortOrder)).all();
}

/** Returns all phases in order, each with the ids of the phases it depends on. */
export function listPhases() {
  const db = getDb();
  const deps = db.select().from(phaseDependencies).all();
  return db
    .select()
    .from(phases)
    .orderBy(asc(phases.sortOrder))
    .all()
    .map((p) => ({ ...p, dependsOn: deps.filter((d) => d.phaseId === p.id).map((d) => d.dependsOnId) }));
}

/** Returns all people sorted by name. */
export function listPeople() {
  return getDb().select().from(people).orderBy(asc(people.name)).all();
}

/**
 * Returns systems matching `filter` with owner names and task counts, in seed order.
 * The owner filter accepts a person id or "none" for unowned systems.
 */
export function listSystems(filter: SystemFilter = {}): SystemListItem[] {
  const db = getDb();
  const conditions = [];
  if (filter.phase) conditions.push(eq(systems.phaseId, filter.phase));
  if (filter.domain) conditions.push(eq(systems.domainId, filter.domain));
  if (filter.status) conditions.push(eq(systems.status, filter.status as Status));
  if (filter.priority) conditions.push(eq(systems.priority, filter.priority as Priority));
  if (filter.owner === "none") conditions.push(sql`${systems.ownerId} IS NULL`);
  else if (filter.owner) conditions.push(eq(systems.ownerId, Number(filter.owner)));

  const rows = db
    .select({
      id: systems.id,
      domainId: systems.domainId,
      phaseId: systems.phaseId,
      title: systems.title,
      summary: systems.summary,
      status: systems.status,
      priority: systems.priority,
      ownerId: systems.ownerId,
      ownerName: people.name,
    })
    .from(systems)
    .leftJoin(people, eq(systems.ownerId, people.id))
    .where(conditions.length ? and(...conditions) : undefined)
    .orderBy(asc(systems.sortOrder))
    .all();

  const counts = db
    .select({
      systemId: tasks.systemId,
      total: count(),
      done: sql<number>`SUM(CASE WHEN ${tasks.status} = 'Done' THEN 1 ELSE 0 END)`,
    })
    .from(tasks)
    .groupBy(tasks.systemId)
    .all();
  const bySystem = new Map(counts.map((c) => [c.systemId, c]));

  return rows.map((r) => ({
    ...r,
    tasksTotal: bySystem.get(r.id)?.total ?? 0,
    tasksDone: Number(bySystem.get(r.id)?.done ?? 0),
  }));
}

/** Returns one system with its tasks and owner, or `undefined` if unknown. */
export function getSystem(id: string) {
  const db = getDb();
  const system = db.select().from(systems).where(eq(systems.id, id)).get();
  if (!system) return undefined;
  const taskRows = db
    .select({
      id: tasks.id,
      title: tasks.title,
      status: tasks.status,
      priority: tasks.priority,
      ownerId: tasks.ownerId,
      ownerName: people.name,
    })
    .from(tasks)
    .leftJoin(people, eq(tasks.ownerId, people.id))
    .where(eq(tasks.systemId, id))
    .orderBy(asc(tasks.sortOrder), asc(tasks.id))
    .all();
  const domain = db.select().from(domains).where(eq(domains.id, system.domainId)).get();
  const phase = db.select().from(phases).where(eq(phases.id, system.phaseId)).get();
  const questions = db.select().from(openQuestions).where(eq(openQuestions.systemId, id)).all();
  return { system, tasks: taskRows, domain, phase, questions };
}

/** Returns the change history of a system and its tasks, newest first. */
export function systemHistory(systemId: string, taskIds: number[]) {
  const db = getDb();
  const taskMatch = taskIds.length
    ? and(eq(changeLog.entity, "task"), inArray(changeLog.entityId, taskIds.map(String)))
    : undefined;
  const systemMatch = and(eq(changeLog.entity, "system"), eq(changeLog.entityId, systemId));
  return db
    .select()
    .from(changeLog)
    .where(taskMatch ? or(systemMatch, taskMatch) : systemMatch)
    .orderBy(desc(changeLog.id))
    .limit(100)
    .all();
}

/** Returns the most recent changes across the whole app. */
export function recentChanges(limit = 50) {
  return getDb().select().from(changeLog).orderBy(desc(changeLog.id)).limit(limit).all();
}

/** Returns all decisions, newest first. */
export function listDecisions() {
  return getDb().select().from(decisions).orderBy(desc(decisions.date), asc(decisions.id)).all();
}

/** Returns all open questions with the title of their related system. */
export function listQuestions() {
  return getDb()
    .select({
      id: openQuestions.id,
      title: openQuestions.title,
      text: openQuestions.text,
      resolved: openQuestions.resolved,
      systemId: openQuestions.systemId,
      systemTitle: systems.title,
    })
    .from(openQuestions)
    .leftJoin(systems, eq(openQuestions.systemId, systems.id))
    .orderBy(asc(openQuestions.resolved), asc(openQuestions.id))
    .all();
}

/** Filters accepted by {@link listUpdates}. */
export interface UpdateFilter {
  systemId?: string;
  limit?: number;
}

/** Returns progress updates, newest first, with system and task titles. */
export function listUpdates(filter: UpdateFilter = {}) {
  return getDb()
    .select({
      id: progressUpdates.id,
      systemId: progressUpdates.systemId,
      systemTitle: systems.title,
      taskId: progressUpdates.taskId,
      taskTitle: tasks.title,
      summary: progressUpdates.summary,
      nextStep: progressUpdates.nextStep,
      commitHash: progressUpdates.commitHash,
      author: progressUpdates.author,
      agent: progressUpdates.agent,
      createdAt: progressUpdates.createdAt,
    })
    .from(progressUpdates)
    .innerJoin(systems, eq(progressUpdates.systemId, systems.id))
    .leftJoin(tasks, eq(progressUpdates.taskId, tasks.id))
    .where(filter.systemId ? eq(progressUpdates.systemId, filter.systemId) : undefined)
    .orderBy(desc(progressUpdates.id))
    .limit(Math.min(Math.max(filter.limit ?? 50, 1), 500))
    .all();
}

/** Returns the newest progress update of every system that has one, keyed by system id. */
export function latestUpdates(): Map<string, { summary: string; author: string; createdAt: string }> {
  const rows = getDb()
    .select({
      systemId: progressUpdates.systemId,
      summary: progressUpdates.summary,
      author: progressUpdates.author,
      createdAt: progressUpdates.createdAt,
    })
    .from(progressUpdates)
    .orderBy(desc(progressUpdates.id))
    .all();
  const latest = new Map<string, { summary: string; author: string; createdAt: string }>();
  for (const r of rows) if (!latest.has(r.systemId)) latest.set(r.systemId, r);
  return latest;
}
