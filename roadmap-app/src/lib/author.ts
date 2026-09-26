"use client";

import { useSyncExternalStore } from "react";

/** Local storage key holding the visitor's display name. */
export const AUTHOR_KEY = "roadmap.displayName";

/** Reads the stored display name, or an empty string when unavailable. */
export function readAuthor(): string {
  try {
    return localStorage.getItem(AUTHOR_KEY) ?? "";
  } catch {
    return "";
  }
}

/** Stores the display name; failures (blocked storage) are ignored. */
export function writeAuthor(name: string): void {
  try {
    localStorage.setItem(AUTHOR_KEY, name);
  } catch {
    // Storage unavailable: changes will be attributed to "unknown".
  }
}

/** Subscribes to display name changes from other tabs. */
function subscribe(callback: () => void): () => void {
  window.addEventListener("storage", callback);
  return () => window.removeEventListener("storage", callback);
}

/** Returns the visitor's display name, re-rendering when it changes in another tab. */
export function useAuthor(): string {
  return useSyncExternalStore(subscribe, readAuthor, () => "");
}
