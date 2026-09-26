import { describe, expect, it } from "vitest";
import { createSession, tokensMatch, verifySession } from "./auth";

describe("tokensMatch", () => {
  it("accepts identical tokens", () => {
    expect(tokensMatch("s3cret-token", "s3cret-token")).toBe(true);
  });

  it("rejects a different token of the same length", () => {
    expect(tokensMatch("s3cret-token", "s3cret-tokeX")).toBe(false);
  });

  it("rejects a token of a different length", () => {
    expect(tokensMatch("s3cret-token", "s3cret")).toBe(false);
  });

  it("rejects an empty expected token", () => {
    expect(tokensMatch("", "")).toBe(false);
  });
});

describe("sessions", () => {
  it("verifies a session signed with the same secret", async () => {
    const cookie = await createSession("secret");
    expect(await verifySession(cookie, "secret")).toBe(true);
  });

  it("rejects a session signed with another secret", async () => {
    const cookie = await createSession("old-secret");
    expect(await verifySession(cookie, "new-secret")).toBe(false);
  });

  it("rejects a tampered session id", async () => {
    const cookie = await createSession("secret");
    const [, sig] = cookie.split(".");
    expect(await verifySession(`forged.${sig}`, "secret")).toBe(false);
  });

  it("rejects missing or malformed cookies", async () => {
    expect(await verifySession(undefined, "secret")).toBe(false);
    expect(await verifySession("no-dot", "secret")).toBe(false);
  });

  it("creates a different session id each time", async () => {
    expect(await createSession("secret")).not.toBe(await createSession("secret"));
  });
});
