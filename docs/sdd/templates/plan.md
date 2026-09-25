# Plan NNNN: <what this plan implements>

- **Status:** In progress
- **Date:** YYYY-MM-DD
- **Accepted proposal:** <one line naming the proposal that was accepted in chat>
- **Decision records:** ADR-NNNN, ADR-NNNN (or: none required)

## Goal

What is true when this plan is done, in one paragraph. Written as an observable
end state, not as a list of activities.

## Out of scope

What this plan deliberately does not do. Each line is something a reader might
reasonably have expected to be included.

## Steps

### Step 1 — <imperative title>

**Does:** what changes.

**Ends in:** the observable, verifiable state this step produces.

**Verified by:** the exact command, test, or observation that proves it.

**Pushes:** no

### Step 2 — <imperative title>

**Does:**

**Ends in:**

**Verified by:**

**Pushes:** no

### Step N — Verification

A verification step is a step, not an afterthought. It states the full check that
proves the goal is met, and it is listed here like any other step.

**Does:**

**Ends in:**

**Verified by:**

**Pushes:** no

## Push points

Agents may not push on their own. If any step above sets `Pushes: yes`, it is
listed here with the reason it cannot continue without CI output. If no step
pushes, this section says: none.

## Risk

The step most likely to go wrong, why it is the one, and what the agent does when
it does go wrong.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what
actually happened, and it does not continue past a step whose stated end state was
not reached.
