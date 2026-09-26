"use client";

import { useState, useTransition } from "react";
import { setQuestionResolvedAction } from "@/app/actions";
import { readAuthor } from "@/lib/author";

/** Checkbox marking an open question as resolved or unresolved. */
export function QuestionToggle({ id, resolved, title }: { id: string; resolved: boolean; title: string }) {
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState<string>();

  return (
    <label className="inline-flex items-center gap-2 text-sm">
      <input
        id={`question-${id}`}
        type="checkbox"
        checked={resolved}
        disabled={pending}
        aria-label={`Resolved: ${title}`}
        onChange={(e) =>
          startTransition(async () => {
            const result = await setQuestionResolvedAction(id, e.target.checked, readAuthor());
            setError(result.ok ? undefined : result.error);
          })
        }
      />
      Resolved
      {error && <span className="text-[var(--danger)]">{error}</span>}
    </label>
  );
}
