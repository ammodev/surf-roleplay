import { History } from "@/components/history";
import { recentChanges } from "@/lib/queries";

/** App-wide change log showing the latest changes and who made them. */
export default function ActivityPage() {
  return (
    <div className="flex flex-col gap-4">
      <div>
        <p className="eyebrow">Activity</p>
        <h1 className="text-2xl font-semibold">Recent changes</h1>
      </div>
      <div className="card p-4">
        <History entries={recentChanges(200)} showEntity />
      </div>
    </div>
  );
}
