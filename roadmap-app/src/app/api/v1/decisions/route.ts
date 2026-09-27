import { apiRoute } from "@/lib/api-route";
import { opListDecisions } from "@/lib/agent-ops";

/** Lists decisions, newest first. */
export const GET = apiRoute(() => opListDecisions());
