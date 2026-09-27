import { apiRoute } from "@/lib/api-route";
import { opListPeople } from "@/lib/agent-ops";

/** Lists the team with ids usable as owners. */
export const GET = apiRoute(() => opListPeople());
