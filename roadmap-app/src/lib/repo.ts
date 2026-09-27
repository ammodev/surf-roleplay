/** Base URL commit hashes are linked to, from `REPO_URL`. */
export const REPO_URL = (process.env.REPO_URL ?? "https://github.com/ammodev/surf-roleplay").replace(/\/+$/, "");

/** Returns the web URL of a commit, or `null` without a hash. */
export function commitUrl(hash: string | null | undefined): string | null {
  return hash ? `${REPO_URL}/commit/${hash}` : null;
}
