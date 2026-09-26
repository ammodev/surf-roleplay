import type { Priority, Status } from "./schema";

/** A domain entry in the seed. */
export interface SeedDomain {
  id: string;
  name: string;
  description: string;
}

/** A phase entry in the seed, with the ids of the phases it builds on. */
export interface SeedPhase {
  id: string;
  name: string;
  goal: string;
  dependsOn: string[];
}

/** A system entry in the seed with its specification and initial tasks. */
export interface SeedSystem {
  id: string;
  domain: string;
  phase: string;
  title: string;
  summary: string;
  /** Markdown specification of everything decided for this system. */
  spec: string;
  priority: Priority;
  /** Initial status; defaults to "Not started". */
  status?: Status;
  tasks: string[];
}

/** A decision entry in the seed. */
export interface SeedDecision {
  id: string;
  title: string;
  text: string;
  adr?: string;
  date: string;
}

/** An open question entry in the seed. */
export interface SeedQuestion {
  id: string;
  title: string;
  text: string;
  system?: string;
}

/** The complete, versioned seed content of the roadmap. */
export interface Seed {
  version: number;
  domains: SeedDomain[];
  phases: SeedPhase[];
  systems: SeedSystem[];
  decisions: SeedDecision[];
  questions: SeedQuestion[];
}
