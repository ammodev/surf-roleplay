/** A block of a parsed specification: a paragraph or a bullet list. */
export type SpecBlock = { kind: "p"; text: string } | { kind: "ul"; items: string[] };

/**
 * Parses the small markdown subset used in specs: paragraphs separated by blank
 * lines and `- ` bullet lists whose indented lines continue the previous bullet.
 *
 * @param spec the markdown text
 * @return the blocks in order
 */
export function parseSpec(spec: string): SpecBlock[] {
  const blocks: SpecBlock[] = [];
  let paragraph: string[] = [];
  let list: string[] | null = null;

  const flushParagraph = () => {
    if (paragraph.length) blocks.push({ kind: "p", text: paragraph.join(" ") });
    paragraph = [];
  };
  const flushList = () => {
    if (list?.length) blocks.push({ kind: "ul", items: list });
    list = null;
  };

  for (const raw of spec.split("\n")) {
    const line = raw.trimEnd();
    if (!line.trim()) {
      flushParagraph();
      flushList();
    } else if (line.startsWith("- ")) {
      flushParagraph();
      list ??= [];
      list.push(line.slice(2).trim());
    } else if (list && /^\s+/.test(line)) {
      list[list.length - 1] += " " + line.trim();
    } else {
      flushList();
      paragraph.push(line.trim());
    }
  }
  flushParagraph();
  flushList();
  return blocks;
}
