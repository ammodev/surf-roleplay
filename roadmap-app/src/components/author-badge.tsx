"use client";

import { useAuthor } from "@/lib/author";

/** Shows the display name changes are attributed to. */
export function AuthorBadge() {
  const author = useAuthor();
  return (
    <span className="text-[var(--ink-2)]" title="Changes are recorded under this name">
      {author || "unknown"}
    </span>
  );
}
