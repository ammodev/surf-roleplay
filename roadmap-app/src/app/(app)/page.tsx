import Link from "next/link";
import { OwnerChip, PriorityChip, StatusChip } from "@/components/chips";
import { PRIORITIES, STATUSES } from "@/db/schema";
import { listDomains, listPeople, listPhases, listSystems, type SystemFilter } from "@/lib/queries";

/** Reads a single string search parameter. */
function param(value: string | string[] | undefined): string {
  return typeof value === "string" ? value : "";
}

/**
 * System catalogue grouped by domain, filterable by phase, status, priority and owner.
 *
 * @param props.searchParams the active filters
 */
export default async function CataloguePage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const sp = await searchParams;
  const filter: SystemFilter = {
    phase: param(sp.phase),
    status: param(sp.status),
    priority: param(sp.priority),
    owner: param(sp.owner),
  };
  const domains = listDomains();
  const phases = listPhases();
  const people = listPeople();
  const systems = listSystems(filter);
  const phaseName = new Map(phases.map((p) => [p.id, p.name]));
  const all = listSystems();
  const done = all.filter((s) => s.status === "Done").length;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">Catalogue</p>
          <h1 className="text-2xl font-semibold">Gamemode systems</h1>
          <p className="text-sm text-[var(--ink-2)] num mt-1">
            {all.length} systems in {domains.length} domains · {done} done
          </p>
        </div>
        <form className="flex flex-wrap gap-2 items-end" method="get">
          <select id="f-phase" name="phase" defaultValue={filter.phase} className="select" aria-label="Phase">
            <option value="">All phases</option>
            {phases.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          <select id="f-status" name="status" defaultValue={filter.status} className="select" aria-label="Status">
            <option value="">All statuses</option>
            {STATUSES.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
          <select id="f-priority" name="priority" defaultValue={filter.priority} className="select" aria-label="Priority">
            <option value="">All priorities</option>
            {PRIORITIES.map((p) => (
              <option key={p}>{p}</option>
            ))}
          </select>
          <select id="f-owner" name="owner" defaultValue={filter.owner} className="select" aria-label="Owner">
            <option value="">Any owner</option>
            <option value="none">Unowned</option>
            {people.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          <button type="submit" className="btn">
            Filter
          </button>
          <Link href="/" className="btn">
            Reset
          </Link>
        </form>
      </div>

      {domains.map((d) => {
        const inDomain = systems.filter((s) => s.domainId === d.id);
        if (inDomain.length === 0) return null;
        return (
          <section key={d.id} className="flex flex-col gap-2">
            <div className="flex items-baseline gap-3">
              <h2 className="text-lg font-semibold">{d.name}</h2>
              <span className="text-sm text-[var(--ink-3)]">{d.description}</span>
            </div>
            <ul className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
              {inDomain.map((s) => (
                <li key={s.id}>
                  <Link href={`/systems/${s.id}`} className="card p-3 flex flex-col gap-2 h-full hover:border-[var(--accent)]">
                    <div className="flex items-start justify-between gap-2">
                      <span className="font-semibold">{s.title}</span>
                      <span className="eyebrow shrink-0">{phaseName.get(s.phaseId)?.split(" ")[0]}</span>
                    </div>
                    <p className="text-sm text-[var(--ink-2)] flex-1">{s.summary}</p>
                    <div className="flex flex-wrap gap-1.5 items-center">
                      <StatusChip status={s.status} />
                      <PriorityChip priority={s.priority} />
                      <OwnerChip name={s.ownerName} />
                      <span className="text-xs text-[var(--ink-3)] num ml-auto">
                        {s.tasksDone}/{s.tasksTotal} tasks
                      </span>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        );
      })}
      {systems.length === 0 && <p className="text-[var(--ink-2)]">No systems match these filters.</p>}
    </div>
  );
}
