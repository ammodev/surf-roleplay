import { eq, max } from "drizzle-orm";
import type { RoadmapDb } from "@/db/database";
import {
  changeLog,
  openQuestions,
  people,
  progressUpdates,
  PRIORITIES,
  ROLES,
  STATUSES,
  systems,
  tasks,
  type Priority,
  type Role,
  type Status,
} from "@/db/schema";

/** Editable fields of a system. */
export interface SystemPatch {
  status?: Status;
  priority?: Priority;
  ownerId?: number | null;
  notes?: string;
}

/** Editable fields of a task. */
export interface TaskPatch {
  title?: string;
  status?: Status;
  priority?: Priority;
  ownerId?: number | null;
}

/** Editable fields of a person. */
export interface PersonInput {
  name: string;
  role: Role;
}

/** Returns a trimmed author name, or "unknown" when none was given. */
function authorOf(author: string): string {
  return author.trim().slice(0, 40) || "unknown";
}

/** Throws unless `value` is one of `allowed`. */
function assertOneOf<T extends string>(value: string, allowed: readonly T[], what: string): asserts value is T {
  if (!allowed.includes(value as T)) throw new Error(`Unknown ${what}: ${value}`);
}

/** Validates the enum fields of a patch. */
function validate(patch: { status?: string; priority?: string }): void {
  if (patch.status !== undefined) assertOneOf(patch.status, STATUSES, "status");
  if (patch.priority !== undefined) assertOneOf(patch.priority, PRIORITIES, "priority");
}

/** Resolves a person id to their name for readable log entries. */
function personName(db: RoadmapDb, id: number | null | undefined): string | null {
  if (id == null) return null;
  return db.select({ name: people.name }).from(people).where(eq(people.id, id)).get()?.name ?? String(id);
}

/** Appends one change log entry. */
function logChange(
  db: RoadmapDb,
  entity: string,
  entityId: string | number,
  field: string,
  oldValue: string | null,
  newValue: string | null,
  author: string,
): void {
  db.insert(changeLog)
    .values({
      entity,
      entityId: String(entityId),
      field,
      oldValue,
      newValue,
      author: authorOf(author),
      createdAt: new Date().toISOString(),
    })
    .run();
}

/**
 * Applies `patch` to a system and logs every field whose value changed.
 * Owner changes are logged by person name.
 *
 * @throws Error if the system does not exist or a value is invalid
 */
export function updateSystem(db: RoadmapDb, id: string, patch: SystemPatch, author: string): void {
  validate(patch);
  db.transaction((tx) => {
    const current = tx.select().from(systems).where(eq(systems.id, id)).get();
    if (!current) throw new Error(`Unknown system: ${id}`);
    const changes: SystemPatch = {};

    if (patch.status !== undefined && patch.status !== current.status) {
      changes.status = patch.status;
      logChange(tx, "system", id, "status", current.status, patch.status, author);
    }
    if (patch.priority !== undefined && patch.priority !== current.priority) {
      changes.priority = patch.priority;
      logChange(tx, "system", id, "priority", current.priority, patch.priority, author);
    }
    if (patch.ownerId !== undefined && patch.ownerId !== current.ownerId) {
      changes.ownerId = patch.ownerId;
      logChange(tx, "system", id, "owner", personName(tx, current.ownerId), personName(tx, patch.ownerId), author);
    }
    if (patch.notes !== undefined && patch.notes !== current.notes) {
      changes.notes = patch.notes;
      logChange(tx, "system", id, "notes", current.notes, patch.notes, author);
    }
    if (Object.keys(changes).length > 0) tx.update(systems).set(changes).where(eq(systems.id, id)).run();
  });
}

/**
 * Adds a task at the end of a system's task list, inheriting the system's priority.
 *
 * @return the new task's id
 * @throws Error if the title is empty or the system does not exist
 */
export function addTask(db: RoadmapDb, systemId: string, title: string, author: string): number {
  const clean = title.trim();
  if (!clean) throw new Error("A task needs a title.");
  return db.transaction((tx) => {
    const system = tx.select().from(systems).where(eq(systems.id, systemId)).get();
    if (!system) throw new Error(`Unknown system: ${systemId}`);
    const [{ last }] = tx.select({ last: max(tasks.sortOrder) }).from(tasks).where(eq(tasks.systemId, systemId)).all();
    const row = tx
      .insert(tasks)
      .values({
        systemId,
        title: clean,
        status: "Not started",
        priority: system.priority,
        ownerId: null,
        sortOrder: (last ?? -1) + 1,
      })
      .returning({ id: tasks.id })
      .get();
    logChange(tx, "task", row.id, "created", null, clean, author);
    return row.id;
  });
}

/**
 * Applies `patch` to a task and logs every field whose value changed.
 *
 * @throws Error if the task does not exist or a value is invalid
 */
export function updateTask(db: RoadmapDb, id: number, patch: TaskPatch, author: string): void {
  validate(patch);
  db.transaction((tx) => {
    const current = tx.select().from(tasks).where(eq(tasks.id, id)).get();
    if (!current) throw new Error(`Unknown task: ${id}`);
    const changes: TaskPatch = {};

    if (patch.title !== undefined && patch.title.trim() && patch.title.trim() !== current.title) {
      changes.title = patch.title.trim();
      logChange(tx, "task", id, "title", current.title, changes.title, author);
    }
    if (patch.status !== undefined && patch.status !== current.status) {
      changes.status = patch.status;
      logChange(tx, "task", id, "status", current.status, patch.status, author);
    }
    if (patch.priority !== undefined && patch.priority !== current.priority) {
      changes.priority = patch.priority;
      logChange(tx, "task", id, "priority", current.priority, patch.priority, author);
    }
    if (patch.ownerId !== undefined && patch.ownerId !== current.ownerId) {
      changes.ownerId = patch.ownerId;
      logChange(tx, "task", id, "owner", personName(tx, current.ownerId), personName(tx, patch.ownerId), author);
    }
    if (Object.keys(changes).length > 0) tx.update(tasks).set(changes).where(eq(tasks.id, id)).run();
  });
}

/** Deletes a task and logs its title. Unknown ids are ignored. */
export function deleteTask(db: RoadmapDb, id: number, author: string): void {
  db.transaction((tx) => {
    const current = tx.select().from(tasks).where(eq(tasks.id, id)).get();
    if (!current) return;
    tx.delete(tasks).where(eq(tasks.id, id)).run();
    logChange(tx, "task", id, "deleted", current.title, null, author);
  });
}

/**
 * Adds a person to the team list.
 *
 * @return the new person's id
 * @throws Error if the name is empty or taken, or the role is unknown
 */
export function addPerson(db: RoadmapDb, input: PersonInput, author: string): number {
  const name = input.name.trim();
  if (!name) throw new Error("A person needs a name.");
  assertOneOf(input.role, ROLES, "role");
  return db.transaction((tx) => {
    const row = tx.insert(people).values({ name, role: input.role }).returning({ id: people.id }).get();
    logChange(tx, "person", row.id, "created", null, `${name} (${input.role})`, author);
    return row.id;
  });
}

/**
 * Renames a person or changes their role.
 *
 * @throws Error if the person does not exist, the name is empty or taken, or the role is unknown
 */
export function updatePerson(db: RoadmapDb, id: number, input: PersonInput, author: string): void {
  const name = input.name.trim();
  if (!name) throw new Error("A person needs a name.");
  assertOneOf(input.role, ROLES, "role");
  db.transaction((tx) => {
    const current = tx.select().from(people).where(eq(people.id, id)).get();
    if (!current) throw new Error(`Unknown person: ${id}`);
    if (current.name !== name) logChange(tx, "person", id, "name", current.name, name, author);
    if (current.role !== input.role) logChange(tx, "person", id, "role", current.role, input.role, author);
    tx.update(people).set({ name, role: input.role }).where(eq(people.id, id)).run();
  });
}

/** Removes a person, clearing their ownership of systems and tasks. Unknown ids are ignored. */
export function deletePerson(db: RoadmapDb, id: number, author: string): void {
  db.transaction((tx) => {
    const current = tx.select().from(people).where(eq(people.id, id)).get();
    if (!current) return;
    tx.update(systems).set({ ownerId: null }).where(eq(systems.ownerId, id)).run();
    tx.update(tasks).set({ ownerId: null }).where(eq(tasks.ownerId, id)).run();
    tx.delete(people).where(eq(people.id, id)).run();
    logChange(tx, "person", id, "deleted", current.name, null, author);
  });
}

/** Marks an open question as resolved or unresolved and logs the change. */
export function setQuestionResolved(db: RoadmapDb, id: string, resolved: boolean, author: string): void {
  db.transaction((tx) => {
    const current = tx.select().from(openQuestions).where(eq(openQuestions.id, id)).get();
    if (!current) throw new Error(`Unknown question: ${id}`);
    if (current.resolved === resolved) return;
    tx.update(openQuestions).set({ resolved }).where(eq(openQuestions.id, id)).run();
    logChange(tx, "question", id, "resolved", String(current.resolved), String(resolved), author);
  });
}

/** Content of a progress update. */
export interface UpdateInput {
  systemId: string;
  taskId?: number | null;
  summary: string;
  nextStep?: string | null;
  commit?: string | null;
}

/** Who posted a progress update. */
export interface UpdateAuthor {
  author: string;
  agent: boolean;
}

/** Content of a new open question. */
export interface QuestionInput {
  title: string;
  text: string;
  systemId?: string | null;
}

/**
 * Formats the author of an agent change as `<agent> (for <person>)`.
 *
 * @throws Error if the agent or the person is empty
 */
export function agentAuthor(agent: string, onBehalfOf: string): string {
  const a = agent.trim();
  const p = onBehalfOf.trim();
  if (!a) throw new Error("`agent` is required: the name of the agent making the change.");
  if (!p) throw new Error("`onBehalfOf` is required: the person the agent works for.");
  return `${a.slice(0, 40)} (for ${p.slice(0, 40)})`;
}

/**
 * Stores a progress update for a system and logs it. The commit hash may name a
 * commit that is not pushed yet.
 *
 * @return the new update's id
 * @throws Error if the summary is empty, the system is unknown, the task belongs to
 *         another system, or the commit hash is not 7 to 40 hex characters
 */
export function postUpdate(db: RoadmapDb, input: UpdateInput, by: UpdateAuthor): number {
  const summary = input.summary.trim();
  if (!summary) throw new Error("An update needs a summary.");
  const commit = input.commit?.trim().toLowerCase() || null;
  if (commit && !/^[0-9a-f]{7,40}$/.test(commit)) throw new Error("`commit` must be a 7 to 40 character hex hash.");

  return db.transaction((tx) => {
    const system = tx.select({ id: systems.id }).from(systems).where(eq(systems.id, input.systemId)).get();
    if (!system) throw new Error(`Unknown system: ${input.systemId}`);
    if (input.taskId != null) {
      const task = tx.select({ systemId: tasks.systemId }).from(tasks).where(eq(tasks.id, input.taskId)).get();
      if (!task || task.systemId !== input.systemId) {
        throw new Error(`Task ${input.taskId} does not belong to system ${input.systemId}.`);
      }
    }
    const row = tx
      .insert(progressUpdates)
      .values({
        systemId: input.systemId,
        taskId: input.taskId ?? null,
        summary,
        nextStep: input.nextStep?.trim() || null,
        commitHash: commit,
        author: authorOf(by.author),
        agent: by.agent,
        createdAt: new Date().toISOString(),
      })
      .returning({ id: progressUpdates.id })
      .get();
    logChange(tx, "update", row.id, "posted", null, summary, by.author);
    return row.id;
  });
}

/**
 * Creates an unresolved open question, optionally linked to a system.
 *
 * @return the new question's id
 * @throws Error if the title is empty or the system is unknown
 */
export function addQuestion(db: RoadmapDb, input: QuestionInput, author: string): string {
  const title = input.title.trim();
  if (!title) throw new Error("A question needs a title.");
  return db.transaction((tx) => {
    if (input.systemId) {
      const system = tx.select({ id: systems.id }).from(systems).where(eq(systems.id, input.systemId)).get();
      if (!system) throw new Error(`Unknown system: ${input.systemId}`);
    }
    const id = `q-${crypto.randomUUID().slice(0, 8)}`;
    tx.insert(openQuestions)
      .values({ id, title, text: input.text.trim(), systemId: input.systemId ?? null, resolved: false })
      .run();
    logChange(tx, "question", id, "created", null, title, author);
    return id;
  });
}
