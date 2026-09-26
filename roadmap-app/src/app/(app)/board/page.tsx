import Link from "next/link";
import { Board } from "@/components/board";
import { listDomains, listPhases, listSystems } from "@/lib/queries";

/** Reads a single string search parameter. */
function param(value: string | string[] | undefined): string {
  return typeof value === "string" ? value : "";
}

/**
 * Kanban board of all systems, filterable by phase and domain.
 *
 * @param props.searchParams the active filters
 */
export default async function BoardPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const sp = await searchParams;
  const phase = param(sp.phase);
  const domain = param(sp.domain);
  const phases = listPhases();
  const phaseLabel = new Map(phases.map((p) => [p.id, p.name.split(" ")[0]]));
  const cards = listSystems({ phase, domain }).map((s) => ({
    id: s.id,
    title: s.title,
    status: s.status,
    priority: s.priority,
    ownerName: s.ownerName,
    phaseLabel: phaseLabel.get(s.phaseId) ?? "",
  }));

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">Board</p>
          <h1 className="text-2xl font-semibold">Systems by status</h1>
          <p className="text-sm text-[var(--ink-2)]">Drag a card to another column, or use its menu.</p>
        </div>
        <form className="flex flex-wrap gap-2" method="get">
          <select id="b-phase" name="phase" defaultValue={phase} className="select" aria-label="Phase">
            <option value="">All phases</option>
            {phases.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          <select id="b-domain" name="domain" defaultValue={domain} className="select" aria-label="Domain">
            <option value="">All domains</option>
            {listDomains().map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>
          <button type="submit" className="btn">
            Filter
          </button>
          <Link href="/board" className="btn">
            Reset
          </Link>
        </form>
      </div>
      <Board cards={cards} />
    </div>
  );
}
