---
name: surf-roadmap
description: How to read and update the hosted surf-roleplay roadmap so the whole team sees what agents are working on - connecting to its MCP server or REST API, finding the right system and task, and when to set status, post progress updates with commit hashes, and raise blockers. Use at the start of any gamemode work, after every commit, when finishing a task, and when blocked.
---

# surf-roleplay roadmap

The roadmap app lists every gamemode system with its specification, tasks, status,
owner and progress updates. The team watches it to see what is happening. Keep it
current while you work, as described below.

## Connect

**MCP (preferred).** This repository ships the server in `.mcp.json` as
`surf-roadmap`. It reads two environment variables, which the human sets before
starting Claude Code:

- `ROADMAP_TOKEN` (required)
- `ROADMAP_URL` (defaults to `http://localhost:3000`; set it to the hosted URL, such
  as `https://roadmap.example.com`)

Claude Code asks once to approve the project server. Never commit the token.

Outside this repository, add the server by hand:

```bash
claude mcp add --transport http surf-roadmap https://<roadmap-host>/api/mcp \
  --header "Authorization: Bearer <ROADMAP_TOKEN>"
```

Tools:

- Read: `list_systems`, `get_system`, `list_phases`, `list_updates`,
  `list_people`, `list_decisions`, `list_questions`
- Write: `update_system`, `add_task`, `update_task`, `post_update`, `add_question`

**REST fallback.** Use this when MCP is not available. Base URL
`https://<roadmap-host>/api/v1`, header `Authorization: Bearer <ROADMAP_TOKEN>`,
JSON bodies.

| Method and path | Purpose |
| --- | --- |
| `GET /systems?phase=&domain=&status=&priority=` | list systems |
| `GET /systems/{id}` | spec, tasks (with ids), questions, updates |
| `PATCH /systems/{id}` | `status`, `priority`, `ownerId`, `notes` |
| `POST /systems/{id}/tasks` | `title` |
| `PATCH /tasks/{id}` | `title`, `status`, `priority`, `ownerId` |
| `POST /systems/{id}/updates` | `summary`, `nextStep`, `taskId`, `commit` |
| `GET /systems/{id}/updates`, `GET /updates?systemId=&limit=` | updates |
| `GET /phases`, `/decisions`, `/people`, `/questions` | reference data |
| `POST /questions` | `title`, `text`, `systemId` |

```bash
curl -s -X POST "$ROADMAP_URL/api/v1/systems/launcher/updates" \
  -H "Authorization: Bearer $ROADMAP_TOKEN" -H "Content-Type: application/json" \
  -d '{"summary":"Added Microsoft login","commit":"1a2b3c4","agent":"Claude Code","onBehalfOf":"Ammo"}'
```

Errors: 401 means a missing or wrong token, 400 an invalid body (the message says
which field), 404 an unknown id.

## Attribution: always send it

Every write needs `agent` (your name, for example `Claude Code`) and `onBehalfOf`
(the human you work for). The roadmap shows `Claude Code (for Ammo)` with an agent
badge. If you do not know the human's name, ask once and keep using it.

## Find what you are working on

1. `list_systems` (filter by `phase` or `domain`), then `get_system` for the
   candidate. System ids are kebab-case, such as `client-mod`, `vehicle-engine` or
   `leitstelle`.
2. Match your task to one of the system's tasks. If none fits, create one with
   `add_task`.
3. The system's `spec` is the agreed specification. If your work contradicts it,
   stop and ask the human instead of changing the spec.

## When to update

| Moment | Do |
| --- | --- |
| **Start** of work on a system or task | `update_system` with `status: "In progress"` (unless it already is); `update_task` with `status: "In progress"` |
| **After each commit** | `post_update` with a 1–3 sentence `summary`, `commit` (the hash, even if not pushed yet), `taskId` when it belongs to a task, and `nextStep` when there is one |
| **Task finished** | `update_task` with `status: "Done"` |
| **System finished** | `update_system` with `status: "Review"` for a human to check, or `"Done"` when the human confirmed it or the plan says so |
| **Blocked** | `update_system` with `status: "Blocked"`, `post_update` explaining why, and `add_question` with the open question linked to the system |

Rules:

- One update per commit. Don't post updates without substance ("working on it").
- Summaries say what changed for the project, not how you felt about it. English only.
- Never set another person's work to Done. Only touch systems and tasks you are
  working on.
- Use `ownerId` only when the human asks you to assign someone (`list_people` gives
  the ids).
- The roadmap's statuses are `Not started`, `Design`, `In progress`, `Review`,
  `Done`, `Blocked`. Priorities are `MVP`, `Later`, `Nice to have`.

## If the roadmap is unreachable

Keep working, and note the updates you could not post (system, summary, commit).
Post them once the roadmap is reachable again, oldest first, and tell the human
that the roadmap was down.
