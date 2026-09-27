import { NextResponse, type NextRequest } from "next/server";
import { SESSION_COOKIE, verifySession } from "@/lib/auth";

/**
 * Redirects every request without a valid session cookie to `/login`.
 *
 * @param request the incoming request
 */
export async function middleware(request: NextRequest) {
  const token = process.env.ROADMAP_TOKEN ?? "";
  const valid = token !== "" && (await verifySession(request.cookies.get(SESSION_COOKIE)?.value, token));
  if (valid) return NextResponse.next();

  const url = request.nextUrl.clone();
  url.pathname = "/login";
  url.search = "";
  return NextResponse.redirect(url);
}

/** Applies the guard to everything except the login page, the bearer-authenticated API and static assets. */
export const config = {
  matcher: ["/((?!login|api/|_next/static|_next/image|favicon.ico).*)"],
};
