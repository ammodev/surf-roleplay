import { apiRoute, parseBody } from "@/lib/api-route";
import { opUpdateTask, updateTaskInput } from "@/lib/agent-ops";

/** Updates a task's title, status, priority or owner. Requires `agent` and `onBehalfOf`. */
export const PATCH = apiRoute(async (request, { id }) => {
  const { id: _ignored, ...shape } = updateTaskInput;
  void _ignored;
  const taskId = Number(id);
  if (!Number.isInteger(taskId)) throw new Error(`Unknown task: ${id}`);
  return opUpdateTask({ ...(await parseBody(request, shape)), id: taskId });
});
