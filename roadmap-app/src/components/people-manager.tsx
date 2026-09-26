"use client";

import { useState, useTransition } from "react";
import { addPersonAction, deletePersonAction, updatePersonAction, type ActionResult } from "@/app/actions";
import { ROLES, type Role } from "@/db/schema";
import { readAuthor } from "@/lib/author";

/** A person row with how many systems they own. */
export interface PersonRow {
  id: number;
  name: string;
  role: Role;
  owned: number;
}

/** One editable person row with rename, role change and a two-step remove. */
function PersonItem({ person, act }: { person: PersonRow; act: (fn: (a: string) => Promise<ActionResult>) => void }) {
  const [name, setName] = useState(person.name);
  const [confirm, setConfirm] = useState(false);

  return (
    <li className="flex flex-wrap items-center gap-2 px-3 py-2">
      <input
        id={`person-name-${person.id}`}
        aria-label={`Name of ${person.name}`}
        className="input flex-1 min-w-40"
        value={name}
        onChange={(e) => setName(e.target.value)}
        onBlur={() => name.trim() !== person.name && act((a) => updatePersonAction(person.id, { name, role: person.role }, a))}
      />
      <select
        id={`person-role-${person.id}`}
        aria-label={`Role of ${person.name}`}
        className="select"
        value={person.role}
        onChange={(e) => act((a) => updatePersonAction(person.id, { name: person.name, role: e.target.value as Role }, a))}
      >
        {ROLES.map((r) => (
          <option key={r}>{r}</option>
        ))}
      </select>
      <span className="text-xs text-[var(--ink-3)] num w-24">{person.owned} systems</span>
      {confirm ? (
        <span className="flex items-center gap-1 text-sm">
          <span className="text-[var(--ink-2)]">Clears their ownership.</span>
          <button type="button" className="btn btn-danger" onClick={() => act((a) => deletePersonAction(person.id, a))}>
            Remove
          </button>
          <button type="button" className="btn" onClick={() => setConfirm(false)}>
            Keep
          </button>
        </span>
      ) : (
        <button type="button" className="btn" onClick={() => setConfirm(true)}>
          Remove
        </button>
      )}
    </li>
  );
}

/** Lists the team with inline editing and a form to add people. */
export function PeopleManager({ people }: { people: PersonRow[] }) {
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState<string>();
  const [name, setName] = useState("");
  const [role, setRole] = useState<Role>("dev");

  /** Runs an action and shows any error. */
  const act = (fn: (author: string) => Promise<ActionResult>) =>
    startTransition(async () => {
      const result = await fn(readAuthor());
      setError(result.ok ? undefined : result.error);
    });

  return (
    <div className="flex flex-col gap-3" aria-busy={pending}>
      <form
        className="card p-3 flex flex-wrap gap-2"
        onSubmit={(e) => {
          e.preventDefault();
          const input = { name, role };
          setName("");
          act((a) => addPersonAction(input, a));
        }}
      >
        <input
          id="new-person-name"
          aria-label="Name"
          placeholder="Name"
          className="input flex-1 min-w-40"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <select id="new-person-role" aria-label="Role" className="select" value={role} onChange={(e) => setRole(e.target.value as Role)}>
          {ROLES.map((r) => (
            <option key={r}>{r}</option>
          ))}
        </select>
        <button type="submit" className="btn btn-primary" disabled={pending || !name.trim()}>
          Add person
        </button>
      </form>
      {error && (
        <p role="alert" className="text-sm text-[var(--danger)]">
          {error}
        </p>
      )}
      <ul className="card divide-y divide-[var(--line)]">
        {people.map((p) => (
          <PersonItem key={`${p.id}-${p.name}-${p.role}`} person={p} act={act} />
        ))}
        {people.length === 0 && <li className="px-3 py-2 text-sm text-[var(--ink-3)]">No people yet. Add the team above.</li>}
      </ul>
    </div>
  );
}
