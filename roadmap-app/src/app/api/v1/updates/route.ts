import { z } from "zod";
import { apiRoute, query } from "@/lib/api-route";
import { listUpdatesInput, opListUpdates } from "@/lib/agent-ops";

/** Lists progress updates across the project, newest first; `systemId` and `limit` query parameters. */
export const GET = apiRoute((request) => {
  const q = query(request, "systemId", "limit");
  return opListUpdates(
    z.object(listUpdatesInput).parse({ systemId: q.systemId, limit: q.limit ? Number(q.limit) : undefined }),
  );
});
