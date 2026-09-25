# Proposal checklist

**A proposal is delivered in the chat as prose. It is never committed as a file.**

This template is a checklist for what that prose must contain. It exists so the
shape of a proposal is reviewable; it is not a document to fill in and save. If a
file matching this template ever appears under `docs/`, it is a mistake — delete
it and put the content back in the conversation.

A proposal covers:

- [ ] **What is being built**, in the human's terms, not in implementation terms.
- [ ] **The approach**, in enough detail that the human can disagree with it.
- [ ] **What changes** — which parts of the codebase are touched, and roughly how.
- [ ] **What does not change** — the scope boundary, stated explicitly.
- [ ] **Decisions that need the human**, each with its real options and real
      trade-offs. Each of these becomes an ADR once decided.
- [ ] **Open questions** still unanswered after the question rounds.
- [ ] **What could go wrong**, honestly.

A proposal is a question, not a green light:

- Implementation does not start while a proposal is on the table.
- No plan is written until the proposal is accepted.
- A rejected proposal returns to the question step. The agent asks what was wrong
  before proposing anything else.
