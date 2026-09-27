import { apiRoute, parseBody } from "@/lib/api-route";
import { opListUpdates, opPostUpdate, postUpdateInput } from "@/lib/agent-ops";

/** Lists the system's progress updates, newest first. */
export const GET = apiRoute((_request, { id }) => opListUpdates({ systemId: id }));

/** Posts a progress update. Body: `summary`, optional `nextStep`, `taskId`, `commit`, plus `agent`, `onBehalfOf`. */
export const POST = apiRoute(async (request, { id }) => {
  const { systemId: _ignored, ...shape } = postUpdateInput;
  void _ignored;
  return opPostUpdate({ ...(await parseBody(request, shape)), systemId: id });
});
