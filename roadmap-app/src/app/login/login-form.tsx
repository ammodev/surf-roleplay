"use client";

import { useActionState, useEffect, useState } from "react";
import { login, type LoginState } from "./actions";
import { readAuthor, writeAuthor } from "@/lib/author";

/** Form collecting the token and display name; the name is kept in local storage. */
export function LoginForm() {
  const [state, action, pending] = useActionState<LoginState, FormData>(login, {});
  const [name, setName] = useState("");
  useEffect(() => setName(readAuthor()), []);

  return (
    <form
      action={action}
      onSubmit={() => writeAuthor(name.trim())}
      className="card flex flex-col gap-4 p-5"
    >
      <label className="flex flex-col gap-1">
        <span className="label">Display name</span>
        <input
          id="displayName"
          name="displayName"
          required
          maxLength={40}
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="input"
          autoComplete="nickname"
        />
      </label>
      <label className="flex flex-col gap-1">
        <span className="label">Access token</span>
        <input id="token" name="token" type="password" required className="input" autoComplete="current-password" />
      </label>
      {state.error && (
        <p role="alert" className="text-sm text-[var(--danger)]">
          {state.error}
        </p>
      )}
      <button type="submit" disabled={pending} className="btn btn-primary">
        {pending ? "Signing in…" : "Sign in"}
      </button>
    </form>
  );
}
