import { apiRoute, parseBody } from "@/lib/api-route";
import { addTaskInput, opAddTask } from "@/lib/agent-ops";

/** Adds a task to the system. Body: `title`, `agent`, `onBehalfOf`. */
export const POST = apiRoute(async (request, { id }) => {
  const { systemId: _ignored, ...shape } = addTaskInput;
  void _ignored;
  return opAddTask({ ...(await parseBody(request, shape)), systemId: id });
});
