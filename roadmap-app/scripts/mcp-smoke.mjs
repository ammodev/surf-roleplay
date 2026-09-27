// Smoke test for the roadmap MCP endpoint.
// Usage: node scripts/mcp-smoke.mjs <base-url> <token> [--write]
// Lists the tools, reads one system and, with --write, posts a test progress update.
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";

const [base = "http://localhost:3000", token = "", flag] = process.argv.slice(2);

/**
 * Connects an MCP client to `<base>/api/mcp` with the bearer token.
 *
 * @param {string} bearer the token to send
 * @returns {Promise<Client>} the connected client
 */
async function connect(bearer) {
  const client = new Client({ name: "roadmap-smoke", version: "1.0.0" });
  const transport = new StreamableHTTPClientTransport(new URL("/api/mcp", base), {
    requestInit: { headers: { Authorization: `Bearer ${bearer}` } },
  });
  await client.connect(transport);
  return client;
}

/** Runs the smoke test and prints what it saw. */
async function main() {
  try {
    await connect("wrong-token");
    console.log("wrong token: CONNECTED (unexpected)");
    process.exitCode = 1;
  } catch (error) {
    console.log("wrong token: rejected", String(error.message ?? error).slice(0, 80));
  }

  const client = await connect(token);
  const { tools } = await client.listTools();
  console.log(`tools (${tools.length}):`, tools.map((t) => t.name).join(", "));

  const system = await client.callTool({ name: "get_system", arguments: { id: "launcher" } });
  const parsed = JSON.parse(system.content[0].text);
  console.log("get_system:", parsed.system.title, "|", parsed.system.status, "|", parsed.tasks.length, "tasks");

  if (flag === "--write") {
    const posted = await client.callTool({
      name: "post_update",
      arguments: {
        systemId: "launcher",
        summary: "MCP smoke test update",
        commit: "abcdef1",
        agent: "roadmap-smoke",
        onBehalfOf: "CI",
      },
    });
    console.log("post_update:", posted.isError ? "ERROR " : "", posted.content[0].text.replace(/\s+/g, " "));
  }
  await client.close();
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
