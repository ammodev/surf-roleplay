import { Fragment } from "react";
import { parseSpec } from "@/lib/spec";

/** Renders inline `code` spans inside a line of text. */
function Inline({ text }: { text: string }) {
  const parts = text.split(/(`[^`]+`)/g);
  return (
    <>
      {parts.map((part, i) =>
        part.startsWith("`") && part.endsWith("`") ? (
          <code key={i}>{part.slice(1, -1)}</code>
        ) : (
          <Fragment key={i}>{part}</Fragment>
        ),
      )}
    </>
  );
}

/** Renders a system specification written in the spec markdown subset. */
export function SpecView({ spec }: { spec: string }) {
  return (
    <div className="prose-spec">
      {parseSpec(spec).map((block, i) =>
        block.kind === "p" ? (
          <p key={i}>
            <Inline text={block.text} />
          </p>
        ) : (
          <ul key={i}>
            {block.items.map((item, j) => (
              <li key={j}>
                <Inline text={item} />
              </li>
            ))}
          </ul>
        ),
      )}
    </div>
  );
}
