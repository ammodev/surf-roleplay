import { integer, primaryKey, sqliteTable, text } from "drizzle-orm/sqlite-core";

/** Workflow states a system or task can be in, in board order. */
export const STATUSES = ["Not started", "Design", "In progress", "Review", "Done", "Blocked"] as const;

/** A workflow state of a system or task. */
export type Status = (typeof STATUSES)[number];

/** Priority classes, from most to least urgent. */
export const PRIORITIES = ["MVP", "Later", "Nice to have"] as const;

/** A priority class of a system or task. */
export type Priority = (typeof PRIORITIES)[number];

/** Roles a person on the team can have. */
export const ROLES = ["dev", "builder", "staff"] as const;

/** The role of a person on the team. */
export type Role = (typeof ROLES)[number];

/** Team members who can own systems and tasks. */
export const people = sqliteTable("people", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  name: text("name").notNull().unique(),
  role: text("role", { enum: ROLES }).notNull(),
});

/** Top-level areas that group systems, such as Police or Vehicles. */
export const domains = sqliteTable("domains", {
  id: text("id").primaryKey(),
  name: text("name").notNull(),
  description: text("description").notNull(),
  sortOrder: integer("sort_order").notNull(),
});

/** Delivery phases of the roadmap, in order. */
export const phases = sqliteTable("phases", {
  id: text("id").primaryKey(),
  name: text("name").notNull(),
  goal: text("goal").notNull(),
  sortOrder: integer("sort_order").notNull(),
});

/** Edges stating that one phase builds on another. */
export const phaseDependencies = sqliteTable(
  "phase_dependencies",
  {
    phaseId: text("phase_id").notNull().references(() => phases.id),
    dependsOnId: text("depends_on_id").notNull().references(() => phases.id),
  },
  (t) => [primaryKey({ columns: [t.phaseId, t.dependsOnId] })],
);

/** Gamemode systems with their specification and tracking fields. */
export const systems = sqliteTable("systems", {
  id: text("id").primaryKey(),
  domainId: text("domain_id").notNull().references(() => domains.id),
  phaseId: text("phase_id").notNull().references(() => phases.id),
  title: text("title").notNull(),
  summary: text("summary").notNull(),
  spec: text("spec").notNull(),
  status: text("status", { enum: STATUSES }).notNull(),
  priority: text("priority", { enum: PRIORITIES }).notNull(),
  ownerId: integer("owner_id").references(() => people.id, { onDelete: "set null" }),
  notes: text("notes").notNull(),
  sortOrder: integer("sort_order").notNull(),
});

/** Work items belonging to a system. */
export const tasks = sqliteTable("tasks", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  systemId: text("system_id").notNull().references(() => systems.id, { onDelete: "cascade" }),
  title: text("title").notNull(),
  status: text("status", { enum: STATUSES }).notNull(),
  priority: text("priority", { enum: PRIORITIES }).notNull(),
  ownerId: integer("owner_id").references(() => people.id, { onDelete: "set null" }),
  sortOrder: integer("sort_order").notNull(),
});

/** Decisions already taken, optionally backed by an ADR. */
export const decisions = sqliteTable("decisions", {
  id: text("id").primaryKey(),
  title: text("title").notNull(),
  text: text("text").notNull(),
  adr: text("adr"),
  date: text("date").notNull(),
});

/** Questions that still need an answer, optionally tied to a system. */
export const openQuestions = sqliteTable("open_questions", {
  id: text("id").primaryKey(),
  title: text("title").notNull(),
  text: text("text").notNull(),
  systemId: text("system_id").references(() => systems.id, { onDelete: "set null" }),
  resolved: integer("resolved", { mode: "boolean" }).notNull(),
});

/** Progress reports posted by people or agents about their work on a system. */
export const progressUpdates = sqliteTable("progress_updates", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  systemId: text("system_id").notNull().references(() => systems.id, { onDelete: "cascade" }),
  taskId: integer("task_id").references(() => tasks.id, { onDelete: "set null" }),
  summary: text("summary").notNull(),
  nextStep: text("next_step"),
  commitHash: text("commit_hash"),
  author: text("author").notNull(),
  agent: integer("agent", { mode: "boolean" }).notNull(),
  createdAt: text("created_at").notNull(),
});

/** Append-only record of every change made through the app. */
export const changeLog = sqliteTable("change_log", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  entity: text("entity").notNull(),
  entityId: text("entity_id").notNull(),
  field: text("field").notNull(),
  oldValue: text("old_value"),
  newValue: text("new_value"),
  author: text("author").notNull(),
  createdAt: text("created_at").notNull(),
});
