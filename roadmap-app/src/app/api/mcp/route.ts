import { WebStandardStreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/webStandardStreamableHttp.js";
import { authorized, json } from "@/lib/api-route";
import { createRoadmapMcpServer } from "@/lib/mcp-server";

/** The endpoint streams and reads the database, so it always runs on Node per request. */
export const dynamic = "force-dynamic";

/**
 * Handles one MCP request statelessly: checks the bearer token, then serves it
 * with a fresh server and transport.
 *
 * @param request the incoming MCP HTTP request
 */
async function handle(request: Request): Promise<Response> {
  if (!authorized(request)) {
    return json({ error: "Missing or wrong bearer token. Send Authorization: Bearer <ROADMAP_TOKEN>." }, 401);
  }
  const server = createRoadmapMcpServer();
  const transport = new WebStandardStreamableHTTPServerTransport({
    sessionIdGenerator: undefined,
    enableJsonResponse: true,
  });
  await server.connect(transport);
  return transport.handleRequest(request);
}

/** MCP requests (initialize, tool calls). */
export const POST = handle;

/** MCP server-to-client stream requests. */
export const GET = handle;

/** MCP session termination requests. */
export const DELETE = handle;
