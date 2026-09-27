# ADR-0025: Agents use the roadmap app through a remote MCP endpoint

- **Status:** Accepted
- **Date:** 2026-09-27
- **Supersedes:** none
- **Superseded by:** none

## Context

Coding agents work on the gamemode on several people's computers. The team wants
agents to read the roadmap and to record their progress in the hosted roadmap app
(ADR-0023), so everyone can see what the project is doing. Agents need a
machine-friendly interface, and it must work without installing anything beyond
their MCP configuration.

## Decision

The hosted roadmap app serves a Model Context Protocol endpoint at `/api/mcp`
using streamable HTTP in stateless mode, built on the official
`@modelcontextprotocol/sdk`. Next to it, a JSON REST API under `/api/v1` offers
the same operations for clients without MCP. Both reuse the app's mutation
functions, so every change lands in the change log.

## Alternatives considered

### A local stdio MCP server that calls the REST API

Its advantage: it works with MCP clients that only support stdio servers, and the
hosted app would only need a REST API.

It was rejected because every agent machine would have to install and update a
local package, while a remote endpoint needs only a URL and a header.

### REST API with instructions only

Its advantage: no MCP dependency at all, and any client that can make HTTP
requests can use it.

It was rejected because agents discover and call MCP tools more reliably than
hand-written curl commands.

## Consequences

### What this gives us

One URL that any MCP-capable agent can add, with typed tools, plus a REST
fallback.

### What this costs

A runtime dependency on the MCP SDK, which evolves quickly. The tools and the
REST routes also have to stay in sync with each other.

### Follow-on work

Tool definitions, REST routes, a skill telling agents when to update the
roadmap, and setup documentation.

### What this forecloses

MCP clients that support only stdio cannot connect directly without a bridge.
