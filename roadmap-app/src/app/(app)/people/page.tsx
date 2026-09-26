import { PeopleManager } from "@/components/people-manager";
import { listPeople, listSystems } from "@/lib/queries";

/** Team list: developers, builders and staff who can own systems and tasks. */
export default function PeoplePage() {
  const systems = listSystems();
  const people = listPeople().map((p) => ({
    ...p,
    owned: systems.filter((s) => s.ownerId === p.id).length,
  }));

  return (
    <div className="flex flex-col gap-4 max-w-3xl">
      <div>
        <p className="eyebrow">People</p>
        <h1 className="text-2xl font-semibold">Team</h1>
        <p className="text-sm text-[var(--ink-2)]">People listed here can be assigned as owners of systems and tasks.</p>
      </div>
      <PeopleManager people={people} />
    </div>
  );
}
