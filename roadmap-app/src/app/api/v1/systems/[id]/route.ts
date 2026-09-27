import { z } from "zod";
import { apiRoute, parseBody } from "@/lib/api-route";
import { opGetSystem, opUpdateSystem, updateSystemInput } from "@/lib/agent-ops";

/** Returns one system with spec, tasks, open questions and recent updates. */
export const GET = apiRoute((_request, { id }) => opGetSystem({ id }));

/** Updates status, priority, owner or notes of a system. Requires `agent` and `onBehalfOf`. */
export const PATCH = apiRoute(async (request, { id }) => {
  const { id: _ignored, ...shape } = updateSystemInput;
  void _ignored;
  const body = await parseBody(request, shape);
  return opUpdateSystem(z.object(updateSystemInput).parse({ ...body, id }));
});
