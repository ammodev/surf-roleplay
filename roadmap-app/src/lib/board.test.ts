import { describe, expect, it } from "vitest";
import { groupByStatus, phaseProgress } from "./board";

describe("groupByStatus", () => {
  it("returns one column per status in board order, keeping item order", () => {
    const cols = groupByStatus([
      { id: "a", status: "Done" as const },
      { id: "b", status: "Not started" as const },
      { id: "c", status: "Done" as const },
    ]);
    expect(cols.map((c) => c.status)).toEqual(["Not started", "Design", "In progress", "Review", "Done", "Blocked"]);
    expect(cols[0].items.map((i) => i.id)).toEqual(["b"]);
    expect(cols[4].items.map((i) => i.id)).toEqual(["a", "c"]);
    expect(cols[1].items).toEqual([]);
  });
});

describe("phaseProgress", () => {
  it("is the share of done systems, rounded to whole percent", () => {
    expect(phaseProgress([{ status: "Done" }, { status: "Design" }, { status: "Done" }])).toBe(67);
  });

  it("is 0 for a phase without systems", () => {
    expect(phaseProgress([])).toBe(0);
  });
});
