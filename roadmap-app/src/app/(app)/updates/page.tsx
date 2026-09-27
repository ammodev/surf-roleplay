import { UpdateList } from "@/components/update-list";
import { listUpdates } from "@/lib/queries";

/** Global feed of progress updates posted by people and agents. */
export default function UpdatesPage() {
  return (
    <div className="flex flex-col gap-4 max-w-3xl">
      <div>
        <p className="eyebrow">Updates</p>
        <h1 className="text-2xl font-semibold">What the project is up to</h1>
        <p className="text-sm text-[var(--ink-2)]">Progress posted by agents and people, newest first.</p>
      </div>
      <UpdateList updates={listUpdates({ limit: 200 })} showSystem />
    </div>
  );
}
