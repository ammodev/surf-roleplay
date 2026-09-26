import Link from "next/link";
import { QuestionToggle } from "@/components/question-toggle";
import { listDecisions, listQuestions } from "@/lib/queries";

/** Decisions already taken and questions that still need an answer. */
export default function DecisionsPage() {
  const decisions = listDecisions();
  const questions = listQuestions();
  const open = questions.filter((q) => !q.resolved).length;

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_24rem]">
      <section className="flex flex-col gap-3 min-w-0">
        <div>
          <p className="eyebrow">Decisions</p>
          <h1 className="text-2xl font-semibold">What has been decided</h1>
        </div>
        <ul className="card divide-y divide-[var(--line)]">
          {decisions.map((d) => (
            <li key={d.id} className="p-3 flex flex-col gap-1">
              <div className="flex flex-wrap items-baseline gap-2">
                {d.adr && <span className="chip mono">{d.adr}</span>}
                <span className="font-semibold">{d.title}</span>
                <time className="mono text-xs text-[var(--ink-3)] ml-auto" dateTime={d.date}>
                  {d.date}
                </time>
              </div>
              <p className="text-sm text-[var(--ink-2)]">{d.text}</p>
            </li>
          ))}
        </ul>
      </section>
      <section className="flex flex-col gap-3">
        <div>
          <p className="eyebrow">Open questions</p>
          <h2 className="text-xl font-semibold num">{open} open</h2>
        </div>
        <ul className="flex flex-col gap-2">
          {questions.map((q) => (
            <li key={q.id} className="card p-3 flex flex-col gap-2">
              <span className={`font-semibold ${q.resolved ? "line-through text-[var(--ink-3)]" : ""}`}>{q.title}</span>
              <p className="text-sm text-[var(--ink-2)]">{q.text}</p>
              <div className="flex flex-wrap items-center justify-between gap-2">
                {q.systemId ? (
                  <Link href={`/systems/${q.systemId}`} className="text-sm text-[var(--accent)] hover:underline">
                    {q.systemTitle}
                  </Link>
                ) : (
                  <span />
                )}
                <QuestionToggle id={q.id} resolved={q.resolved} title={q.title} />
              </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
