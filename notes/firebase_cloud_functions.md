# Firebase Cloud Functions — Per-Note Reminders

Server-side code that runs on Google's infrastructure in response to events (Firestore writes, HTTP,
schedules) or on a timer. We use it to build **per-note reminders**: a scheduled function scans notes
for due reminders and sends a **data-only** FCM push that deep-links back to the note.

Why Cloud Functions at all: the Firebase console can only send *notification* messages (which bypass
`onMessageReceived` in the background). **Data-only** messages — which always hit `onMessageReceived`
— can only be sent via the Admin SDK, i.e. from a server / Cloud Function.

## Why a notes app even needs notifications
A single-user notes app has no "user X did something, notify user Y" event, so generic pushes would be
spam. The one notification that genuinely serves the user is a **reminder they set themselves**. That's
the feature: user-requested, high value, never nagging.

---

## Milestone 0 — Scaffold

Stood up a `functions/` codebase (no product logic yet) and wired it into the project.

| Piece | Choice |
|---|---|
| Language | **Python** — no build step, and the rest of this project has nothing invested in Node/TS |
| SDK | `firebase_functions` **v2** (2nd gen) + `firebase_admin` (`requirements.txt`) |
| Runtime | **Python 3.12**, pinned explicitly (see gotcha below) |
| Wiring | `functions` block in `firebase.json` + a `functions` emulator on `:5001` |

### Why Python over the "default" TypeScript
TypeScript is the Firebase docs' default, but that's a convention, not a requirement — 2nd-gen
Functions is a first-class GA runtime in **both** languages, and everything this feature needs
(`on_schedule`, Firestore collection-group queries, `messaging.send` with a data payload) exists in the
Python SDK too. Since the rest of Notey is 100% Kotlin, there was no "keep one language" argument for
TS specifically — Python's no-build-step simplicity won for a small scheduled job like this.

### 1st vs 2nd gen
We use **2nd-gen** functions: built on Cloud Run, better concurrency, and the `on_schedule` /
`on_document_created` / `on_call` decorators with native Cloud Scheduler for cron. That's what the
reminder scheduler (M5) needs.

### The runtime-pin gotcha
With no `runtime` set, the Firebase CLI silently picks `supported.latest("python")` — currently
**python3.14** — and tries to run `python3.14` inside our venv, which only has 3.12:

```
Failed to find location of Firebase Functions SDK. Did you forget to run
'. venv/bin/activate && python3.14 -m pip install -r requirements.txt'?
```

Fix: pin the runtime explicitly in `firebase.json`:
```json
"functions": [{ "source": "functions", "codebase": "default", "runtime": "python312" }]
```
**Lesson:** never rely on "latest" defaults for a deploy runtime — pin it, so an SDK release next
month can't silently change what ships.

### Local env
A local `venv/` (Python 3.12) with `pip install -r requirements.txt`; `.gitignore` excludes
`venv/`/`__pycache__/`. Unlike the abandoned TS attempt, **no gitignore rescue was needed** — the repo
root's blanket `*.json` rule doesn't touch `requirements.txt` or `main.py`.

### Blaze vs emulator
Deploying functions needs the **Blaze** (pay-as-you-go) plan — but the **Functions emulator runs
locally without billing**, so we build and test through M5 on the emulator and only enable Blaze at
deploy (M6).

### Verify
- `pip install -r functions/requirements.txt` inside the venv succeeds.
- `firebase emulators:exec --only functions "…"` → `✔ Loaded functions definitions from source`.

---

## Milestone 1 — Reminder fields, end to end

Before any function exists, the note needs somewhere to carry a reminder. Two nullable fields, added
consistently through every layer:

| Field | Meaning |
|---|---|
| `reminderAt: Long?` | When the reminder is due (`null` = no reminder set) |
| `reminderFiredAt: Long?` | `null` while pending; stamped once delivered — the guard against re-firing |

### Every layer, one field name
`Note` (domain) → `NoteEntity` (Room) → `NoteDto` (Firestore) → all 4 mapper functions in
`NoteMappers.kt`. Keeping the field name identical across all four avoids a translation layer and
keeps `toDto()`/`toEntity()` mechanical. Both default to `null`, so every existing call site
(note creation, other mappers, tests) keeps compiling unchanged — additive, not breaking.

### Extending the update path, not duplicating it
`NoteDao.updateNoteFields()` already receives the whole edited `Note` via `updateNote()` in
`NoteRepositoryImpl` — reminders are just two more columns in that same `UPDATE`. Adding a parallel
"update reminder only" method would fork the write path for no reason (the whole note already flows
through here on every edit).

### Room migration — and the gotcha that would've broken it silently
Bumped `AppDatabase` **v5 → v6** with `MIGRATION_5_6` (two `ALTER TABLE … ADD COLUMN` statements,
nullable, `DEFAULT NULL` — the cheapest kind of Room migration, no table rebuild needed unlike the
v3→v4 rename).

**Gotcha:** defining a `Migration` object isn't enough — Room only applies migrations passed to
`.addMigrations(...)` in `DatabaseModule.kt`. `MIGRATION_5_6` had to be added there explicitly; forgetting
this step would compile fine and then crash (or silently mismatch) the first time a real device tries
to open the upgraded database. **Lesson:** a new migration is two edits, not one — define it *and*
register it.

### Firestore round-trip
No extra plumbing needed: `NoteEntity.toDto()` feeds `syncNotes()`'s `.set(entity.toDto())`, and
`NoteDto.toEntity()` feeds the `startRemoteSync()` listener — both already pass every field through, so
`reminderAt`/`reminderFiredAt` sync automatically once present on the DTO.

### Verify
- `compileDevDebugKotlin` succeeds.
- Set a note's `reminderAt` locally → after a sync cycle, the field appears on the Firestore document
  at `users/{uid}/notes/{id}`.
