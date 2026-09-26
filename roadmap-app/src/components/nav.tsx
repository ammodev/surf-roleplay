import Link from "next/link";
import { logout } from "@/app/login/actions";
import { AuthorBadge } from "./author-badge";

/** Navigation entries in display order. */
const LINKS = [
  { href: "/", label: "Catalogue" },
  { href: "/board", label: "Board" },
  { href: "/roadmap", label: "Roadmap" },
  { href: "/decisions", label: "Decisions" },
  { href: "/people", label: "People" },
  { href: "/activity", label: "Activity" },
];

/** Top navigation bar with the app name, section links, the author and logout. */
export function Nav() {
  return (
    <header className="border-b border-[var(--line)] bg-[var(--surface)] sticky top-0 z-10">
      <div className="mx-auto max-w-7xl px-4 py-2 flex flex-wrap items-center gap-x-6 gap-y-2">
        <Link href="/" className="flex items-baseline gap-2">
          <span className="eyebrow">surf-roleplay</span>
          <span className="font-semibold">Roadmap</span>
        </Link>
        <nav className="flex flex-wrap gap-1 text-sm">
          {LINKS.map((l) => (
            <Link key={l.href} href={l.href} className="px-2 py-1 rounded hover:bg-[var(--surface-2)]">
              {l.label}
            </Link>
          ))}
        </nav>
        <div className="ml-auto flex items-center gap-3 text-sm">
          <AuthorBadge />
          <form action={logout}>
            <button type="submit" className="btn">
              Sign out
            </button>
          </form>
        </div>
      </div>
    </header>
  );
}
