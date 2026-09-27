import { describe, expect, it } from "vitest";
import { bearerMatches } from "./bearer";

describe("bearerMatches", () => {
  it("accepts the token as a bearer credential", () => {
    expect(bearerMatches("Bearer s3cret", "s3cret")).toBe(true);
  });

  it("accepts a lowercase scheme and surrounding spaces", () => {
    expect(bearerMatches("  bearer   s3cret ", "s3cret")).toBe(true);
  });

  it("rejects a wrong token", () => {
    expect(bearerMatches("Bearer nope", "s3cret")).toBe(false);
  });

  it("rejects a missing header or another scheme", () => {
    expect(bearerMatches(null, "s3cret")).toBe(false);
    expect(bearerMatches("Basic s3cret", "s3cret")).toBe(false);
  });

  it("rejects everything when no token is configured", () => {
    expect(bearerMatches("Bearer ", "")).toBe(false);
  });
});
