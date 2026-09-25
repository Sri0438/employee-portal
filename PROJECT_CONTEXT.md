# Employee & Admin Management Portal — Project Context

> **How to use this file:** keep it updated at the end of each work session, commit it to the repo, and at the start of a new chat with Claude either paste its contents or say "read `PROJECT_CONTEXT.md` in my repo, let's continue." It's the handoff between sessions so nothing gets re-explained or re-decided.

## How I'm working
I'm a beginner (already know Java/OOP) building this with Claude's help, on a
~5-day self-imposed deadline. Keep the same fast-but-correct style: bigger
batches of complete, runnable code with exact file paths, brief (not
exhaustive) explanations of new concepts, and wait for real terminal
output/screenshots before moving to the next step — don't guess at what
happened. Don't use your own sandbox tools (`bash_tool` etc.) for this project
— they run in a separate sandbox, not my Codespace. Everything must be given
as text/code for me to paste into my own terminal.

## Project scope (MVP)
Two roles: **ADMIN** and **EMPLOYEE**, enforced on the backend (never just
hidden in the UI) — an employee must never be able to see another employee's
data by changing an ID in a URL/request.

- **Employee:** login/logout, clock in/out, view own attendance history, view
  assigned tasks, update task progress.
- **Admin:** login/logout, list/add/edit/activate-deactivate employees, assign
  tasks, view all attendance, basic dashboard counts (tasks by status, etc).

**Deferred to post-MVP** (do not build yet): notifications, email, task
comments/attachments, exports, audit logs, separate roles table, departments,
Manager/HR roles, leave/payroll.

## Tech stack (all free tier, $0 budget)
- **Backend:** Java 25 (Codespace default JDK — originally planned Java 21,
  this is fine, Spring Boot 4.1 supports it) + Spring Boot 4.1.1 + Maven +
  Spring Web + Spring Data JPA (Hibernate) + Spring Security + JWT
  (`io.jsonwebtoken` / jjwt 0.12.6) + PostgreSQL driver + Validation.
- **Frontend:** React + Vite (JavaScript, not TypeScript), folder name
  `frontend`.
- **Database:** PostgreSQL via Neon (free cloud tier) — project already
  created.
- **Dev environment:** 100% browser-based — GitHub Codespaces, no local
  installs at all (on Windows, but nothing installed locally). VS Code runs in
  the browser tab. Extensions installed: "Extension Pack for Java", "Spring
  Boot Extension Pack".
- **Deployment:** Netlify (frontend), Render via Docker (backend), Neon
  (database) — all live.

## Architecture