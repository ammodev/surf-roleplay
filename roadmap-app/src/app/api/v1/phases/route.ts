import { apiRoute } from "@/lib/api-route";
import { opListPhases } from "@/lib/agent-ops";

/** Lists phases in order with their dependencies. */
export const GET = apiRoute(() => opListPhases());
