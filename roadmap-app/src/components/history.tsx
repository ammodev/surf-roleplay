/** A change log entry as rendered in history lists. */
export interface HistoryEntry {
  id: number;
  entity: string;
  entityId: string;
  field: string;
  oldValue: string | null;
  newValue: string | null;
  author: string;
  createdAt: string;
}

/** Shortens long values such as notes for one-line display. */
function short(value: string | null): string {
  if (value == null || value === "") return "—";
  return value.length > 60 ? `${value.slice(0, 57)}…` : value;
}

/** Renders a list of change log entries, newest first. */
export function History({ entries, showEntity = false }: { entries: HistoryEntry[]; showEntity?: boolean }) {
  if (entries.length === 0) return <p className="text-sm text-[var(--ink-3)]">No changes yet.</p>;
  return (
    <ol className="flex flex-col gap-2 text-sm">
      {entries.map((e) => (
        <li key={e.id} className="flex flex-wrap gap-x-2">
          <time className="mono text-xs text-[var(--ink-3)] num" dateTime={e.createdAt}>
            {e.createdAt.slice(0, 16).replace("T", " ")}
          </time>
          <span className="font-semibold">{e.author}</span>
          <span className="text-[var(--ink-2)]">
            {showEntity && (
              <span className="mono text-xs">
                {e.entity}:{e.entityId}{" "}
              </span>
            )}
            {e.field}: {short(e.oldValue)} → {short(e.newValue)}
          </span>
        </li>
      ))}
    </ol>
  );
}
