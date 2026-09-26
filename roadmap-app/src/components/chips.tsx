import type { Priority, Status } from "@/db/schema";

/** CSS colour token for each status. */
export const STATUS_COLOR: Record<Status, string> = {
  "Not started": "var(--st-not-started)",
  Design: "var(--st-design)",
  "In progress": "var(--st-in-progress)",
  Review: "var(--st-review)",
  Done: "var(--st-done)",
  Blocked: "var(--st-blocked)",
};

/** A pill showing a status with its colour dot. */
export function StatusChip({ status }: { status: Status }) {
  return (
    <span className="chip">
      <span className="dot" style={{ background: STATUS_COLOR[status] }} aria-hidden />
      {status}
    </span>
  );
}

/** A pill showing a priority; MVP is emphasised. */
export function PriorityChip({ priority }: { priority: Priority }) {
  return (
    <span
      className="chip mono"
      style={priority === "MVP" ? { borderColor: "var(--accent)", color: "var(--accent)" } : undefined}
    >
      {priority}
    </span>
  );
}

/** A pill showing the owner, or "Unowned". */
export function OwnerChip({ name }: { name: string | null }) {
  return <span className="chip">{name ?? "Unowned"}</span>;
}
