import "server-only";
import { revalidatePath } from "next/cache";
import { z } from "zod";
import { getDb } from "@/db";
import { PRIORITIES, STATUSES } from "@/db/schema";
import {
  addQuestion,
  addTask,
  agentAuthor,
  postUpdate,
  updateSystem,
  updateTask,
} from "./mutations";
import {
  getSystem,
  listDecisions,
  listPeople,
  listPhases,
  listQuestions,
  listSystems,
  listUpdates,
} from "./queries";

/** Base URL commit hashes are linked to. */
export const REPO_URL = (process.env.REPO_URL ?? "https://github.com/ammodev/surf-roleplay").replace(/\/+$/, "");

/** Attribution every agent write must carry. */
export const attribution = {
  agent: z.string().min(1).describe("Name of the agent making the change, e.g. \"Claude Code\"."),
  onBehalfOf: z.string().min(1).describe("Name of the person the agent works for."),
};

/** Input of {@link opListSystems}. */
export const listSystemsInput = {
  phase: z.string().optional().describe("Phase id, e.g. p0."),
  domain: z.string().optional().describe("Domain id, e.g. vehicles."),
  status: z.enum(STATUSES).optional(),
  priority: z.enum(PRIORITIES).optional(),
};

/** Input of {@link opGetSystem}. */
export const getSystemInput = { id: z.string().min(1).describe("System id, e.g. client-mod.") };

/** Input of {@link opListUpdates}. */
export const listUpdatesInput = {
  systemId: z.string().optional(),
  limit: z.number().int().min(1).max(500).optional(),
};

/** Input of {@link opUpdateSystem}. */
export const updateSystemInput = {
  id: z.string().min(1),
  status: z.enum(STATUSES).optional(),
  priority: z.enum(PRIORITIES).optional(),
  ownerId: z.number().int().nullable().optional().describe("Person id from list_people, or null to clear."),
  notes: z.string().optional().describe("Replaces the system's notes."),
  ...attribution,
};

/** Input of {@link opAddTask}. */
export const addTaskInput = { systemId: z.string().min(1), title: z.string().min(1), ...attribution };

/** Input of {@link opUpdateTask}. */
export const updateTaskInput = {
  id: z.number().int(),
  title: z.string().optional(),
  status: z.enum(STATUSES).optional(),
  priority: z.enum(PRIORITIES).optional(),
  ownerId: z.number().int().nullable().optional(),
  ...attribution,
};

/** Input of {@link opPostUpdate}. */
export const postUpdateInput = {
  systemId: z.string().min(1),
  summary: z.string().min(1).describe("What was done, short markdown."),
  nextStep: z.string().optional(),
  taskId: z.number().int().optional().describe("Task of the same system this update belongs to."),
  commit: z.string().optional().describe("Commit hash (7-40 hex); may be unpushed."),
  ...attribution,
};

/** Input of {@link opAddQuestion}. */
export const addQuestionInput = {
  title: z.string().min(1),
  text: z.string().default(""),
  systemId: z.string().optional(),
  ...attribution,
};

/** Converts a zod shape into a parsed-value type. */
type Input<S extends z.ZodRawShape> = z.infer<z.ZodObject<S>>;

/** Refreshes every page after an agent write. */
function refresh(): void {
  try {
    revalidatePath("/", "layout");
  } catch {
    // Outside a request (tests, scripts) there is nothing to revalidate.
  }
}

/** Lists systems with status, priority, owner and task progress. */
export function opListSystems(input: Input<typeof listSystemsInput>) {
  return listSystems(input);
}

/**
 * Returns one system with spec, tasks, open questions and its latest updates.
 *
 * @throws Error `Unknown system` if the id does not exist
 */
export function opGetSystem({ id }: Input<typeof getSystemInput>) {
  const data = getSystem(id);
  if (!data) throw new Error(`Unknown system: ${id}`);
  return { ...data, updates: listUpdates({ systemId: id, limit: 20 }).map(withCommitUrl) };
}

/** Returns phases with dependencies. */
export function opListPhases() {
  return listPhases();
}

/** Returns decisions. */
export function opListDecisions() {
  return listDecisions();
}

/** Returns open questions. */
export function opListQuestions() {
  return listQuestions();
}

/** Returns the team list with ids for owner assignment. */
export function opListPeople() {
  return listPeople();
}

/** Adds the commit URL to an update row. */
function withCommitUrl<T extends { commitHash: string | null }>(u: T): T & { commitUrl: string | null } {
  return { ...u, commitUrl: u.commitHash ? `${REPO_URL}/commit/${u.commitHash}` : null };
}

/** Returns progress updates, newest first. */
export function opListUpdates(input: Input<typeof listUpdatesInput>) {
  return listUpdates(input).map(withCommitUrl);
}

/** Updates a system's status, priority, owner or notes and returns the system. */
export function opUpdateSystem({ id, agent, onBehalfOf, ...patch }: Input<typeof updateSystemInput>) {
  updateSystem(getDb(), id, patch, agentAuthor(agent, onBehalfOf));
  refresh();
  return opGetSystem({ id }).system;
}

/** Adds a task to a system and returns its id. */
export function opAddTask({ systemId, title, agent, onBehalfOf }: Input<typeof addTaskInput>) {
  const id = addTask(getDb(), systemId, title, agentAuthor(agent, onBehalfOf));
  refresh();
  return { id };
}

/** Updates a task's title, status, priority or owner. */
export function opUpdateTask({ id, agent, onBehalfOf, ...patch }: Input<typeof updateTaskInput>) {
  updateTask(getDb(), id, patch, agentAuthor(agent, onBehalfOf));
  refresh();
  return { id };
}

/** Posts a progress update and returns its id and commit link. */
export function opPostUpdate({ agent, onBehalfOf, ...input }: Input<typeof postUpdateInput>) {
  const id = postUpdate(getDb(), input, { author: agentAuthor(agent, onBehalfOf), agent: true });
  refresh();
  const commit = input.commit?.trim().toLowerCase();
  return { id, commitUrl: commit ? `${REPO_URL}/commit/${commit}` : null };
}

/** Adds an open question and returns its id. */
export function opAddQuestion({ agent, onBehalfOf, ...input }: Input<typeof addQuestionInput>) {
  const id = addQuestion(getDb(), input, agentAuthor(agent, onBehalfOf));
  refresh();
  return { id };
}
