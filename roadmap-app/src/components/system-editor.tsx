"use client";

import { useState, useTransition } from "react";
import { updateSystemAction } from "@/app/actions";
import { PRIORITIES, STATUSES, type Priority, type Status } from "@/db/schema";
import { readAuthor } from "@/lib/author";

/** Props of {@link SystemEditor}. */
export interface SystemEditorProps {
  id: string;
  status: Status;
  priority: Priority;
  ownerId: number | null;
  notes: string;
  people: { id: number; name: string }[];
}

/** Edits a system's status, priority, owner and notes; every change is saved immediately except notes. */
export function SystemEditor({ id, status, priority, ownerId, notes, people }: SystemEditorProps) {
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState<string>();
  const [draft, setDraft] = useState(notes);

  /** Saves a patch and shows any error. */
  const save = (patch: Parameters<typeof updateSystemAction>[1]) =>
    startTransition(async () => {
      const result = await updateSystemAction(id, patch, readAuthor());
      setError(result.ok ? undefined : result.error);
    });

  return (
    <div className="card p-4 flex flex-col gap-3" aria-busy={pending}>
      <label className="flex flex-col gap-1">
        <span className="label">Status</span>
        <select
          id={`status-${id}`}
          className="select"
          value={status}
          disabled={pending}
          onChange={(e) => save({ status: e.target.value as Status })}
        >
          {STATUSES.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
      </label>
      <label className="flex flex-col gap-1">
        <span className="label">Priority</span>
        <select
          id={`priority-${id}`}
          className="select"
          value={priority}
          disabled={pending}
          onChange={(e) => save({ priority: e.target.value as Priority })}
        >
          {PRIORITIES.map((p) => (
            <option key={p}>{p}</option>
          ))}
        </select>
      </label>
      <label className="flex flex-col gap-1">
        <span className="label">Owner</span>
        <select
          id={`owner-${id}`}
          className="select"
          value={ownerId ?? ""}
          disabled={pending}
          onChange={(e) => save({ ownerId: e.target.value ? Number(e.target.value) : null })}
        >
          <option value="">Unowned</option>
          {people.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>
      </label>
      <label className="flex flex-col gap-1">
        <span className="label">Notes</span>
        <textarea
          id={`notes-${id}`}
          className="textarea min-h-28"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
        />
      </label>
      <button
        type="button"
        className="btn self-start"
        disabled={pending || draft === notes}
        onClick={() => save({ notes: draft })}
      >
        Save notes
      </button>
      {error && (
        <p role="alert" className="text-sm text-[var(--danger)]">
          {error}
        </p>
      )}
    </div>
  );
}
