"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { getDb } from "@/db";
import { requireToken, SESSION_COOKIE, verifySession } from "@/lib/auth";
import {
  addPerson,
  addTask,
  deletePerson,
  deleteTask,
  setQuestionResolved,
  updatePerson,
  updateSystem,
  updateTask,
  type PersonInput,
  type SystemPatch,
  type TaskPatch,
} from "@/lib/mutations";

/** Result of a mutation: `ok`, or an error message to show. */
export type ActionResult = { ok: true } | { ok: false; error: string };

/**
 * Checks the session cookie, runs `fn`, refreshes every page and converts thrown
 * errors into a result. Actions are reachable independently of the middleware's
 * route matcher, so every mutation verifies the session itself.
 */
async function run(fn: () => void): Promise<ActionResult> {
  const store = await cookies();
  if (!(await verifySession(store.get(SESSION_COOKIE)?.value, requireToken()))) {
    return { ok: false, error: "Your session has ended. Sign in again." };
  }
  try {
    fn();
    revalidatePath("/", "layout");
    return { ok: true };
  } catch (e) {
    return { ok: false, error: e instanceof Error ? e.message : "Something went wrong." };
  }
}

/** Updates status, priority, owner or notes of a system. */
export async function updateSystemAction(id: string, patch: SystemPatch, author: string): Promise<ActionResult> {
  return run(() => updateSystem(getDb(), id, patch, author));
}

/** Adds a task to a system. */
export async function addTaskAction(systemId: string, title: string, author: string): Promise<ActionResult> {
  return run(() => void addTask(getDb(), systemId, title, author));
}

/** Updates a task. */
export async function updateTaskAction(id: number, patch: TaskPatch, author: string): Promise<ActionResult> {
  return run(() => updateTask(getDb(), id, patch, author));
}

/** Deletes a task. */
export async function deleteTaskAction(id: number, author: string): Promise<ActionResult> {
  return run(() => deleteTask(getDb(), id, author));
}

/** Adds a person to the team list. */
export async function addPersonAction(input: PersonInput, author: string): Promise<ActionResult> {
  return run(() => void addPerson(getDb(), input, author));
}

/** Renames a person or changes their role. */
export async function updatePersonAction(id: number, input: PersonInput, author: string): Promise<ActionResult> {
  return run(() => updatePerson(getDb(), id, input, author));
}

/** Removes a person and clears their ownership. */
export async function deletePersonAction(id: number, author: string): Promise<ActionResult> {
  return run(() => deletePerson(getDb(), id, author));
}

/** Marks an open question resolved or unresolved. */
export async function setQuestionResolvedAction(id: string, resolved: boolean, author: string): Promise<ActionResult> {
  return run(() => setQuestionResolved(getDb(), id, resolved, author));
}
