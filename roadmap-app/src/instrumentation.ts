/** Runs once when the server starts and refuses to start without a login token. */
export async function register() {
  if (process.env.NEXT_RUNTIME === "nodejs") {
    const { requireToken } = await import("@/lib/auth");
    requireToken();
  }
}
