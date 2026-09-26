"use client";

import Link from "next/link";
import { useOptimistic, useState, useTransition } from "react";
import { updateSystemAction } from "@/app/actions";
import { STATUSES, type Priority, type Status } from "@/db/schema";
import { readAuthor } from "@/lib/author";
import { groupByStatus } from "@/lib/board";
import { OwnerChip, PriorityChip, STATUS_COLOR } from "./chips";

/** A card on the board. */
export interface BoardCard {
  id: string;
  title: string;
  status: Status;
  priority: Priority;
  ownerName: string | null;
  phaseLabel: string;
}

/**
 * Kanban board with one column per status. Dragging a card to a column, or picking
 * a status from the card's menu, changes the system's status.
 */
export function Board({ cards }: { cards: BoardCard[] }) {
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState<string>();
  const [dragOver, setDragOver] = useState<Status | null>(null);
  const [optimistic, moveOptimistic] = useOptimistic(cards, (state, move: { id: string; status: Status }) =>
    state.map((c) => (c.id === move.id ? { ...c, status: move.status } : c)),
  );

  /** Moves a card to `status` and persists the change. */
  const move = (id: string, status: Status) => {
    const card = optimistic.find((c) => c.id === id);
    if (!card || card.status === status) return;
    startTransition(async () => {
      moveOptimistic({ id, status });
      const result = await updateSystemAction(id, { status }, readAuthor());
      setError(result.ok ? undefined : result.error);
    });
  };

  return (
    <div className="flex flex-col gap-2" aria-busy={pending}>
      {error && (
        <p role="alert" className="text-sm text-[var(--danger)]">
          {error}
        </p>
      )}
      <div className="overflow-x-auto pb-2">
        <div className="grid grid-flow-col auto-cols-[minmax(15rem,1fr)] gap-3">
          {groupByStatus(optimistic).map((col) => (
            <section
              key={col.status}
              aria-label={col.status}
              onDragOver={(e) => {
                e.preventDefault();
                setDragOver(col.status);
              }}
              onDragLeave={() => setDragOver(null)}
              onDrop={(e) => {
                e.preventDefault();
                setDragOver(null);
                move(e.dataTransfer.getData("text/plain"), col.status);
              }}
              className="rounded-lg p-2 flex flex-col gap-2 min-h-40 border"
              style={{
                background: "var(--surface-2)",
                borderColor: dragOver === col.status ? STATUS_COLOR[col.status] : "var(--line)",
              }}
            >
              <h2 className="flex items-center gap-2 text-sm font-semibold px-1">
                <span className="dot" style={{ background: STATUS_COLOR[col.status] }} aria-hidden />
                {col.status}
                <span className="ml-auto text-[var(--ink-3)] num">{col.items.length}</span>
              </h2>
              {col.items.map((c) => (
                <article
                  key={c.id}
                  draggable
                  onDragStart={(e) => e.dataTransfer.setData("text/plain", c.id)}
                  className="card p-2.5 flex flex-col gap-2 cursor-grab active:cursor-grabbing"
                >
                  <div className="flex items-start justify-between gap-2">
                    <Link href={`/systems/${c.id}`} className="font-semibold text-sm hover:underline">
                      {c.title}
                    </Link>
                    <span className="eyebrow shrink-0">{c.phaseLabel}</span>
                  </div>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <PriorityChip priority={c.priority} />
                    <OwnerChip name={c.ownerName} />
                  </div>
                  <select
                    id={`board-status-${c.id}`}
                    aria-label={`Move ${c.title}`}
                    className="select text-xs"
                    value={c.status}
                    onChange={(e) => move(c.id, e.target.value as Status)}
                  >
                    {STATUSES.map((s) => (
                      <option key={s}>{s}</option>
                    ))}
                  </select>
                </article>
              ))}
            </section>
          ))}
        </div>
      </div>
    </div>
  );
}
