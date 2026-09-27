import Link from "next/link";
import { commitUrl } from "@/lib/repo";
import { relativeAge } from "@/lib/time";

/** A progress update as rendered in timelines and the feed. */
export interface UpdateItem {
  id: number;
  systemId: string;
  systemTitle: string;
  taskTitle: string | null;
  summary: string;
  nextStep: string | null;
  commitHash: string | null;
  author: string;
  agent: boolean;
  createdAt: string;
}

/**
 * Renders progress updates as a timeline, newest first.
 *
 * @param props.updates the updates to show
 * @param props.showSystem whether each entry links to its system (for the global feed)
 */
export function UpdateList({ updates, showSystem = false }: { updates: UpdateItem[]; showSystem?: boolean }) {
  if (updates.length === 0) {
    return <p className="text-sm text-[var(--ink-3)]">No progress updates yet.</p>;
  }
  return (
    <ol className="flex flex-col gap-3">
      {updates.map((u) => {
        const url = commitUrl(u.commitHash);
        return (
          <li key={u.id} className="card p-3 flex flex-col gap-1.5">
            <div className="flex flex-wrap items-center gap-2 text-sm">
              <span className="font-semibold">{u.author}</span>
              {u.agent && (
                <span className="chip mono" style={{ borderColor: "var(--st-design)", color: "var(--st-design)" }}>
                  agent
                </span>
              )}
              {showSystem && (
                <Link href={`/systems/${u.systemId}`} className="text-[var(--accent)] hover:underline">
                  {u.systemTitle}
                </Link>
              )}
              <time className="mono text-xs text-[var(--ink-3)] ml-auto" dateTime={u.createdAt} title={u.createdAt}>
                {relativeAge(u.createdAt)}
              </time>
            </div>
            <p className="text-sm whitespace-pre-wrap">{u.summary}</p>
            {u.nextStep && (
              <p className="text-sm text-[var(--ink-2)]">
                <span className="label">Next:</span> {u.nextStep}
              </p>
            )}
            {(u.taskTitle || url) && (
              <div className="flex flex-wrap gap-2 text-xs">
                {u.taskTitle && <span className="chip">Task: {u.taskTitle}</span>}
                {url && (
                  <a href={url} target="_blank" rel="noreferrer" className="chip mono hover:border-[var(--accent)]">
                    {u.commitHash?.slice(0, 7)}
                  </a>
                )}
              </div>
            )}
          </li>
        );
      })}
    </ol>
  );
}
