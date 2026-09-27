import Link from "next/link";
import { notFound } from "next/navigation";
import { History } from "@/components/history";
import { SpecView } from "@/components/spec-view";
import { SystemEditor } from "@/components/system-editor";
import { TaskList } from "@/components/task-list";
import { UpdateList } from "@/components/update-list";
import { getSystem, listPeople, listUpdates, systemHistory } from "@/lib/queries";

/**
 * Detail page of one system: spec, editable tracking fields, tasks and history.
 *
 * @param props.params the route parameters with the system id
 */
export default async function SystemPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const data = getSystem(id);
  if (!data) notFound();
  const { system, tasks, domain, phase, questions } = data;
  const people = listPeople().map((p) => ({ id: p.id, name: p.name }));
  const history = systemHistory(id, tasks.map((t) => t.id));

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link href="/" className="text-sm text-[var(--ink-2)] hover:underline">
          ← Catalogue
        </Link>
        <p className="eyebrow mt-3">
          {domain?.name} · {phase?.name}
        </p>
        <h1 className="text-2xl font-semibold">{system.title}</h1>
        <p className="text-[var(--ink-2)] mt-1 max-w-3xl">{system.summary}</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr_18rem]">
        <div className="flex flex-col gap-6 min-w-0">
          <section className="card p-4">
            <h2 className="text-lg font-semibold mb-2">Specification</h2>
            <SpecView spec={system.spec} />
          </section>
          {questions.length > 0 && (
            <section className="card p-4">
              <h2 className="text-lg font-semibold mb-2">Open questions</h2>
              <ul className="flex flex-col gap-2 text-sm">
                {questions.map((q) => (
                  <li key={q.id} className={q.resolved ? "line-through text-[var(--ink-3)]" : ""}>
                    <span className="font-semibold">{q.title}</span> {q.text}
                  </li>
                ))}
              </ul>
            </section>
          )}
          <section>
            <h2 className="text-lg font-semibold mb-2">Progress updates</h2>
            <UpdateList updates={listUpdates({ systemId: system.id, limit: 100 })} />
          </section>
          <TaskList systemId={system.id} tasks={tasks} people={people} />
          <section>
            <h2 className="text-lg font-semibold mb-2">History</h2>
            <History entries={history} showEntity />
          </section>
        </div>
        <aside>
          <SystemEditor
            key={`${system.status}-${system.priority}-${system.ownerId}-${system.notes}`}
            id={system.id}
            status={system.status}
            priority={system.priority}
            ownerId={system.ownerId}
            notes={system.notes}
            people={people}
          />
        </aside>
      </div>
    </div>
  );
}
