import { describe, expect, it } from "vitest";
import { parseSpec } from "./spec";

describe("parseSpec", () => {
  it("splits paragraphs and bullet lists", () => {
    expect(parseSpec("Intro line.\n\n- one\n- two\n\nOutro.")).toEqual([
      { kind: "p", text: "Intro line." },
      { kind: "ul", items: ["one", "two"] },
      { kind: "p", text: "Outro." },
    ]);
  });

  it("joins indented continuation lines into the previous bullet", () => {
    expect(parseSpec("- first part\n  continues here\n- second")).toEqual([
      { kind: "ul", items: ["first part continues here", "second"] },
    ]);
  });

  it("joins wrapped paragraph lines", () => {
    expect(parseSpec("line one\nline two")).toEqual([{ kind: "p", text: "line one line two" }]);
  });

  it("returns nothing for an empty spec", () => {
    expect(parseSpec("  \n ")).toEqual([]);
  });
});
