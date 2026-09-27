import { apiRoute, parseBody } from "@/lib/api-route";
import { addQuestionInput, opAddQuestion, opListQuestions } from "@/lib/agent-ops";

/** Lists open questions, unresolved first. */
export const GET = apiRoute(() => opListQuestions());

/** Adds an open question. Body: `title`, `text`, optional `systemId`, plus `agent`, `onBehalfOf`. */
export const POST = apiRoute(async (request) => opAddQuestion(await parseBody(request, addQuestionInput)));
