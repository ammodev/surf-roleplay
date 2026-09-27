import { z } from "zod";
import { apiRoute, query } from "@/lib/api-route";
import { listSystemsInput, opListSystems } from "@/lib/agent-ops";

/** Lists systems, filterable by `phase`, `domain`, `status` and `priority` query parameters. */
export const GET = apiRoute((request) =>
  opListSystems(z.object(listSystemsInput).parse(query(request, "phase", "domain", "status", "priority"))),
);
