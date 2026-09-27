import { tokensMatch } from "./auth";

/**
 * Checks an `Authorization` header value for `Bearer <token>`.
 *
 * @param header the header value, if present
 * @param token the configured login token
 * @return whether the header carries exactly the token
 */
export function bearerMatches(header: string | null, token: string): boolean {
  const match = header?.trim().match(/^bearer\s+(.+)$/i);
  if (!match) return false;
  return tokensMatch(token, match[1].trim());
}
