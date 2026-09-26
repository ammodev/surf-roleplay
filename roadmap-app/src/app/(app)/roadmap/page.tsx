import Link from "next/link";
import { StatusChip } from "@/components/chips";
import { phaseProgress } from "@/lib/board";
import { listPhases, listSystems } from "@/lib/queries";

/** Phase roadmap: every phase in order with its goal, dependencies, progress and systems. */
export default function RoadmapPage() {
  const phases = listPhases();
  const systems = listSystems();
  const name = new Map(phases.map((p) => [p.id, p.name]));

  return (
    <div className="flex flex-col gap-4">
      <div>
        <p className="eyebrow">Roadmap</p>
        <h1 className="text-2xl font-semibold">Delivery phases</h1>
      </div>
      <ol className="flex flex-col gap-3">
        {phases.map((p) => {
          const items = systems.filter((s) => s.phaseId === p.id);
          const progress = phaseProgress(items);
          const done = items.filter((s) => s.status === "Done").length;
          return (
            <li key={p.id} className="card p-4 flex flex-col gap-3">
              <div className="flex flex-wrap items-baseline justify-between gap-2">
                <h2 className="text-lg font-semibold">{p.name}</h2>
                <span className="text-sm text-[var(--ink-2)] num">
                  {done}/{items.length} systems done · {progress}%
                </span>
              </div>
              <p className="text-sm text-[var(--ink-2)] max-w-3xl">{p.goal}</p>
              <div
                className="h-2 rounded-full overflow-hidden"
                style={{ background: "var(--surface-2)" }}
                role="progressbar"
                aria-valuenow={progress}
                aria-valuemin={0}
                aria-valuemax={100}
                aria-label={`${p.name} progress`}
              >
                <div className="h-full" style={{ width: `${progress}%`, background: "var(--st-done)" }} />
              </div>
              {p.dependsOn.length > 0 && (
                <p className="text-xs text-[var(--ink-3)]">
                  Builds on: {p.dependsOn.map((d) => name.get(d) ?? d).join(", ")}
                </p>
              )}
              <ul className="flex flex-wrap gap-2">
                {items.map((s) => (
                  <li key={s.id}>
                    <Link
                      href={`/systems/${s.id}`}
                      className="inline-flex items-center gap-2 text-sm border border-[var(--line)] rounded-md px-2 py-1 hover:border-[var(--accent)]"
                    >
                      {s.title}
                      <StatusChip status={s.status} />
                    </Link>
                  </li>
                ))}
              </ul>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
