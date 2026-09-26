import { STATUSES, type Status } from "@/db/schema";

/** A board column: a status and the items in it. */
export interface Column<T> {
  status: Status;
  items: T[];
}

/**
 * Groups items into one column per status, in board order, preserving item order.
 *
 * @param items items carrying a status
 */
export function groupByStatus<T extends { status: Status }>(items: T[]): Column<T>[] {
  return STATUSES.map((status) => ({ status, items: items.filter((i) => i.status === status) }));
}

/**
 * Returns the share of done items as a whole percentage (0 when empty).
 *
 * @param items items carrying a status
 */
export function phaseProgress(items: { status: Status }[]): number {
  if (items.length === 0) return 0;
  return Math.round((items.filter((i) => i.status === "Done").length / items.length) * 100);
}
