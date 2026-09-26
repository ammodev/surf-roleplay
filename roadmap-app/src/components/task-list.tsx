"use client";

import { useState, useTransition } from "react";
import { addTaskAction, deleteTaskAction, updateTaskAction } from "@/app/actions";
import { STATUSES, type Priority, type Status } from "@/db/schema";
import { readAuthor } from "@/lib/author";
import { STATUS_COLOR } from "./chips";

/** A task row as shown in the list. */
export interface TaskRow {
  id: number;
  title: string;
  status: Status;
  priority: Priority;
  ownerId: number | null;
}

/** Lists a system's tasks with inline status/owner editing, deletion and an add form. */
export function TaskList({
  systemId,
  tasks,
  people,
}: {
  systemId: string;
  tasks: TaskRow[];
  people: { id: number; name: string }[];
}) {
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState<string>();
  const [title, setTitle] = useState("");
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null);

  /** Runs an action and shows any error. */
  const act = (fn: (author: string) => ReturnType<typeof deleteTaskAction>) =>
    startTransition(async () => {
      const result = await fn(readAuthor());
      setError(result.ok ? undefined : result.error);
    });

  const done = tasks.filter((t) => t.status === "Done").length;

  return (
    <section className="flex flex-col gap-3" aria-busy={pending}>
      <div className="flex items-baseline justify-between">
        <h2 className="text-lg font-semibold">Tasks</h2>
        <span className="text-sm text-[var(--ink-3)] num">
          {done}/{tasks.length} done
        </span>
      </div>
      <ul className="card divide-y divide-[var(--line)]">
        {tasks.map((t) => (
          <li key={t.id} className="flex flex-wrap items-center gap-2 px-3 py-2">
            <span className="dot" style={{ background: STATUS_COLOR[t.status] }} aria-hidden />
            <span className={`flex-1 min-w-40 ${t.status === "Done" ? "line-through text-[var(--ink-3)]" : ""}`}>
              {t.title}
            </span>
            <select
              id={`task-status-${t.id}`}
              aria-label={`Status of ${t.title}`}
              className="select text-xs"
              value={t.status}
              disabled={pending}
              onChange={(e) => act((a) => updateTaskAction(t.id, { status: e.target.value as Status }, a))}
            >
              {STATUSES.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
            <select
              id={`task-owner-${t.id}`}
              aria-label={`Owner of ${t.title}`}
              className="select text-xs"
              value={t.ownerId ?? ""}
              disabled={pending}
              onChange={(e) =>
                act((a) => updateTaskAction(t.id, { ownerId: e.target.value ? Number(e.target.value) : null }, a))
              }
            >
              <option value="">Unowned</option>
              {people.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
            {confirmDelete === t.id ? (
              <span className="flex gap-1">
                <button
                  type="button"
                  className="btn btn-danger text-xs"
                  onClick={() => {
                    setConfirmDelete(null);
                    act((a) => deleteTaskAction(t.id, a));
                  }}
                >
                  Delete
                </button>
                <button type="button" className="btn text-xs" onClick={() => setConfirmDelete(null)}>
                  Keep
                </button>
              </span>
            ) : (
              <button
                type="button"
                className="btn text-xs"
                aria-label={`Delete ${t.title}`}
                onClick={() => setConfirmDelete(t.id)}
              >
                ✕
              </button>
            )}
          </li>
        ))}
        {tasks.length === 0 && <li className="px-3 py-2 text-sm text-[var(--ink-3)]">No tasks yet.</li>}
      </ul>
      <form
        className="flex gap-2"
        onSubmit={(e) => {
          e.preventDefault();
          const value = title;
          setTitle("");
          act((a) => addTaskAction(systemId, value, a));
        }}
      >
        <input
          id={`new-task-${systemId}`}
          aria-label="New task"
          placeholder="Add a task"
          className="input flex-1"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <button type="submit" className="btn" disabled={pending || !title.trim()}>
          Add
        </button>
      </form>
      {error && (
        <p role="alert" className="text-sm text-[var(--danger)]">
          {error}
        </p>
      )}
    </section>
  );
}
