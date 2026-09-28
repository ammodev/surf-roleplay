# Local dev stack

A Docker Compose project that runs everything the roleplay plugin and client mod need for live
testing:

| Service | What it is | Reachable from the host |
|---|---|---|
| `postgres` | PostgreSQL 17, database `surf`, user `postgres` / `dev` | `localhost:5432` |
| `redis` | Redis 7 | no |
| `rabbitmq` | RabbitMQ 4, user `surf` / `dev` | management UI on `localhost:15672` |
| `fetch` | One-shot job that downloads the jars pinned in `versions.env` into `jars/` | no |
| `ms-core`, `ms-transaction` | surf-core and surf-transaction microservices | no |
| `ms-roleplay` | the roleplay microservice, run from `surf-roleplay-microservice/build/libs` | no |
| `paper` | Paper 26.2 with the surf plugins and `surf-roleplay-paper` | no, only through Velocity |
| `velocity` | Velocity 4.2.0 with modern forwarding and `surf-roleplay-velocity` | `localhost:25565` |

The Minecraft EULA is accepted in `templates/paper/eula.txt`. By using this stack you accept it
too: <https://aka.ms/MinecraftEULA>.

## Prerequisites

- Docker with Compose v2
- JDK 25, to build the project

## Start

```bash
./gradlew build                # from the repository root; the stack runs the built jars
cd tools/devenv
docker compose up -d
docker compose logs -f paper   # wait for "Done (...)! For help, type "help""
```

The first start downloads about 530 MB of jars and generates the world, which takes a few
minutes. Later starts reuse `jars/` and `run/`.

## Join

```bash
./gradlew :surf-roleplay-fabric:runLocalClient
```

This starts the Fabric dev client with the roleplay mod and joins `localhost:25565` directly.
Any other client can join `localhost:25565` too, but without the mod it is disconnected by the
handshake after 10 seconds.

The dev client loads every Fabric API module as a separate mod. Production clients load them
nested inside `fabric-api`. So the dev Paper config lists those module ids in
`handshake.allowed-mods`, in `run/paper/plugins/surf-roleplay-paper/config.yml`.

## Work with it

| Task | Command (in `tools/devenv`) |
|---|---|
| Reload after `./gradlew build` | `docker compose restart paper velocity ms-roleplay` |
| Follow logs | `docker compose logs -f paper` (or any service) |
| Server console | `docker compose attach paper`, detach with `Ctrl+P` `Ctrl+Q` |
| Stop | `docker compose down` |
| Reset worlds, configs and the database | `docker compose down -v`, then delete `run/` |
| Update a pinned version | edit `versions.env`; the next `docker compose up` downloads all jars again |

On every start, Paper and Velocity replace all plugin jars. They get the fetched plugins plus the
newest `*-all.jar` from the matching Gradle build folder.

Files from `templates/` are only copied into `run/` when they do not exist yet. Edits in `run/`
therefore survive restarts. After changing a template, delete the copied file from `run/` or
reset the stack.

## Known pitfalls

- **PostgreSQL, not MariaDB.** surf-core uses upserts with conflict keys, which Exposed does not
  support on MariaDB.
- **Velocity is required.** surf-core-paper only accepts players that the proxy has registered in
  Redis, so joining Paper directly fails.
- **No native Netty transports.** Docker's default seccomp profile blocks io_uring, so every Java
  service runs with `-Dio.netty.transport.noNative=true` and uses NIO.
- **Configuration-phase packets.** Paper drops payloads sent to the client during the configuration
  phase. Server-to-client roleplay packets only work once the player is in the world.
- **Line endings.** The scripts run inside Linux containers, and `.gitattributes` keeps
  `tools/devenv` in LF. If a script fails with `$'\r': command not found`, re-checkout the file.
