"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { createSession, requireToken, SESSION_COOKIE, SESSION_MAX_AGE, tokensMatch } from "@/lib/auth";

/** Result of a login attempt shown by the login form. */
export interface LoginState {
  error?: string;
}

/**
 * Checks the submitted token and, on success, sets the session cookie and
 * redirects to the catalogue.
 *
 * @param _prev the previous form state
 * @param form the submitted form with a `token` field
 */
export async function login(_prev: LoginState, form: FormData): Promise<LoginState> {
  const expected = requireToken();
  const submitted = String(form.get("token") ?? "");
  if (!tokensMatch(expected, submitted)) return { error: "That token is not correct." };

  const store = await cookies();
  store.set(SESSION_COOKIE, await createSession(expected), {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.COOKIE_SECURE === "true",
    path: "/",
    maxAge: SESSION_MAX_AGE,
  });
  redirect("/");
}

/** Clears the session cookie and returns to the login page. */
export async function logout(): Promise<void> {
  const store = await cookies();
  store.delete(SESSION_COOKIE);
  redirect("/login");
}
