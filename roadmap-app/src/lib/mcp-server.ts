import "server-only";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import type { CallToolResult } from "@modelcontextprotocol/sdk/types.js";
import {
  addQuestionInput,
  addTaskInput,
  getSystemInput,
  listSystemsInput,
  listUpdatesInput,
  opAddQuestion,
  opAddTask,
  opGetSystem,
  opListDecisions,
  opListPeople,
  opListPhases,
  opListQuestions,
  opListSystems,
  opListUpdates,
  opPostUpdate,
  opUpdateSystem,
  opUpdateTask,
  postUpdateInput,
  updateSystemInput,
  updateTaskInput,
} from "./agent-ops";

/** Names of every tool the roadmap MCP server exposes. */
export const TOOL_NAMES = [
  "list_systems",
  "get_system",
  "list_phases",
  "list_updates",
  "list_people",
  "list_decisions",
  "list_questions",
  "update_system",
  "add_task",
  "update_task",
  "post_update",
  "add_question",
] as const;

/** Runs a tool body and wraps its result, or its error, as MCP text content. */
async function result(fn: () => unknown): Promise<CallToolResult> {
  try {
    return { content: [{ type: "text", text: JSON.stringify(await fn(), null, 2) }] };
  } catch (error) {
    return { content: [{ type: "text", text: error instanceof Error ? error.message : String(error) }], isError: true };
  }
}

/**
 * Builds an MCP server exposing the roadmap: read tools for systems, phases,
 * updates, people, decisions and questions, and write tools that record agent
 * progress. Writes require `agent` and `onBehalfOf`.
 */
export function createRoadmapMcpServer(): McpServer {
  const server = new McpServer(
    { name: "surf-roadmap", version: "1.0.0" },
    {
      instructions:
        "Roadmap of the surf-roleplay gamemode. Find the system you work on with list_systems or get_system, " +
        "set it In progress when you start, post_update after each commit, mark tasks Done and the system Review or Done " +
        "when finished, and set Blocked plus add_question when stuck. Always pass agent and onBehalfOf on writes.",
    },
  );

  server.registerTool(
    "list_systems",
    { description: "List gamemode systems with status, priority, owner and task progress.", inputSchema: listSystemsInput },
    (input) => result(() => opListSystems(input)),
  );
  server.registerTool(
    "get_system",
    { description: "Get one system: spec, tasks (with ids), open questions and recent updates.", inputSchema: getSystemInput },
    (input) => result(() => opGetSystem(input)),
  );
  server.registerTool("list_phases", { description: "List delivery phases with goals and dependencies." }, () =>
    result(() => opListPhases()),
  );
  server.registerTool(
    "list_updates",
    { description: "List progress updates, newest first, optionally for one system.", inputSchema: listUpdatesInput },
    (input) => result(() => opListUpdates(input)),
  );
  server.registerTool("list_people", { description: "List team members with ids usable as ownerId." }, () =>
    result(() => opListPeople()),
  );
  server.registerTool("list_decisions", { description: "List decisions and ADRs." }, () =>
    result(() => opListDecisions()),
  );
  server.registerTool("list_questions", { description: "List open questions, unresolved first." }, () =>
    result(() => opListQuestions()),
  );
  server.registerTool(
    "update_system",
    { description: "Change a system's status, priority, owner or notes.", inputSchema: updateSystemInput },
    (input) => result(() => opUpdateSystem(input)),
  );
  server.registerTool(
    "add_task",
    { description: "Add a task to a system.", inputSchema: addTaskInput },
    (input) => result(() => opAddTask(input)),
  );
  server.registerTool(
    "update_task",
    { description: "Change a task's title, status, priority or owner (e.g. status Done).", inputSchema: updateTaskInput },
    (input) => result(() => opUpdateTask(input)),
  );
  server.registerTool(
    "post_update",
    {
      description: "Post a progress update for a system: summary, optional next step, task id and commit hash.",
      inputSchema: postUpdateInput,
    },
    (input) => result(() => opPostUpdate(input)),
  );
  server.registerTool(
    "add_question",
    { description: "Add an open question, optionally linked to a system.", inputSchema: addQuestionInput },
    (input) => result(() => opAddQuestion(input)),
  );

  return server;
}
