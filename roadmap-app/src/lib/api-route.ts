import "server-only";
import { z } from "zod";
import { bearerMatches } from "./bearer";

/** Route context carrying the dynamic segments of the URL. */
export interface RouteContext {
  params: Promise<Record<string, string>>;
}

/** Returns a JSON response with `status`. */
export function json(body: unknown, status = 200): Response {
  return Response.json(body, { status });
}

/**
 * Returns whether the request carries the configured token as a bearer credential.
 * An unset token rejects every request.
 */
export function authorized(request: Request): boolean {
  return bearerMatches(request.headers.get("authorization"), process.env.ROADMAP_TOKEN ?? "");
}

/**
 * Parses the JSON body of `request` against a zod shape.
 *
 * @throws z.ZodError when the body does not match
 */
export async function parseBody<S extends z.ZodRawShape>(request: Request, shape: S): Promise<z.infer<z.ZodObject<S>>> {
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    body = {};
  }
  return z.object(shape).parse(body);
}

/** Maps a thrown error to a JSON error response: 404 for unknown ids, 400 otherwise. */
function errorResponse(error: unknown): Response {
  if (error instanceof z.ZodError) {
    return json({ error: "Invalid request", issues: error.issues.map((i) => `${i.path.join(".")}: ${i.message}`) }, 400);
  }
  const message = error instanceof Error ? error.message : "Something went wrong.";
  return json({ error: message }, /^Unknown /.test(message) ? 404 : 400);
}

/**
 * Wraps an API route: rejects requests without the bearer token with 401, runs
 * `handler`, and returns its result as JSON or maps errors to 400/404.
 *
 * @param handler computes the response body from the request and URL params
 */
export function apiRoute(handler: (request: Request, params: Record<string, string>) => unknown | Promise<unknown>) {
  return async (request: Request, context: RouteContext): Promise<Response> => {
    if (!authorized(request)) {
      return json({ error: "Missing or wrong bearer token. Send Authorization: Bearer <ROADMAP_TOKEN>." }, 401);
    }
    try {
      return json(await handler(request, await context.params));
    } catch (error) {
      return errorResponse(error);
    }
  };
}

/** Reads optional query parameters as strings, dropping empty ones. */
export function query(request: Request, ...names: string[]): Record<string, string | undefined> {
  const params = new URL(request.url).searchParams;
  return Object.fromEntries(names.map((n) => [n, params.get(n) || undefined]));
}
