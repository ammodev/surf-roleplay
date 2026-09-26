/** Name of the httpOnly session cookie. */
export const SESSION_COOKIE = "roadmap_session";

/** Lifetime of a session cookie in seconds (30 days). */
export const SESSION_MAX_AGE = 60 * 60 * 24 * 30;

const encoder = new TextEncoder();

/**
 * Compares a submitted token with the expected one in time independent of where
 * they differ. An empty expected token never matches.
 *
 * @param expected the configured token
 * @param submitted the token entered by the visitor
 * @return whether both are equal and non-empty
 */
export function tokensMatch(expected: string, submitted: string): boolean {
  const a = encoder.encode(expected);
  const b = encoder.encode(submitted);
  if (a.length === 0) return false;
  let diff = a.length ^ b.length;
  for (let i = 0; i < a.length; i++) diff |= a[i] ^ (b[i % (b.length || 1)] ?? 0);
  return diff === 0;
}

/** Returns the lowercase hex encoding of `bytes`. */
function toHex(bytes: ArrayBuffer): string {
  return Array.from(new Uint8Array(bytes), (x) => x.toString(16).padStart(2, "0")).join("");
}

/** Computes the hex HMAC-SHA256 of `message` keyed with `secret`. */
async function hmac(message: string, secret: string): Promise<string> {
  const key = await crypto.subtle.importKey("raw", encoder.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, [
    "sign",
  ]);
  return toHex(await crypto.subtle.sign("HMAC", key, encoder.encode(message)));
}

/**
 * Creates a signed session cookie value `<id>.<signature>` with a random id.
 *
 * @param secret the login token used as signing key
 */
export async function createSession(secret: string): Promise<string> {
  const id = crypto.randomUUID();
  return `${id}.${await hmac(id, secret)}`;
}

/**
 * Checks that a session cookie value was signed with `secret`. Rotating the token
 * therefore invalidates every existing session.
 *
 * @param cookie the cookie value, if any
 * @param secret the current login token
 */
export async function verifySession(cookie: string | undefined, secret: string): Promise<boolean> {
  if (!cookie) return false;
  const dot = cookie.lastIndexOf(".");
  if (dot <= 0) return false;
  const expected = await hmac(cookie.slice(0, dot), secret);
  return tokensMatch(expected, cookie.slice(dot + 1));
}

/**
 * Returns the configured login token.
 *
 * @throws Error if `ROADMAP_TOKEN` is unset or empty
 */
export function requireToken(): string {
  const token = process.env.ROADMAP_TOKEN;
  if (!token) throw new Error("ROADMAP_TOKEN is not set; the roadmap app refuses to run without it.");
  return token;
}
