# Firebase Products in Notey — The Complete Map

Notey is a Kotlin/Compose notes app built as a hands-on **Firebase products class**: one product at a
time, each wired into a *real feature* rather than a toy demo. The method throughout: short verified
milestones (compile + on-device where possible), one feature branch per product PR'd into `dev`, and a
teaching note per product capturing not just *how* but *why* — including the bugs found along the way.

**Ten products integrated.** Deep-dive notes live alongside this file:
[Remote Config](firebase_remote_config.md) · [Cloud Functions](firebase_cloud_functions.md) ·
[App Check](firebase_app_check.md) · [Auth token lifetime](firebase-auth-token-refresh-and-session-lifetime.md)

## At a glance

| # | Product | Role in Notey | Key entry point |
| --- | --- | --- | --- |
| 1 | **Authentication** | Email/password + Google sign-in, verification gate, session | `data/auth/AuthRepositoryImpl.kt` |
| 2 | **Cloud Firestore** | Notes + notifications inbox + push tokens (source of truth) | `data/notes/NoteRepositoryImpl.kt` |
| 3 | **Cloud Storage** | Note cover images with upload progress | `data/storage/StorageRepositoryImpl.kt` |
| 4 | **Cloud Messaging** | Data-only pushes: banner, tray, deep links | `data/notifications/AppFirebaseMessagingService.kt` |
| 5 | **Crashlytics** | Crash reports with user context + log breadcrumbs | `core/logging/CrashReportingTree.kt` |
| 6 | **Analytics** | Product events, fanned out to Firebase + Mixpanel | `data/observability/CompositeAnalyticsTracker.kt` |
| 7 | **Performance** | Custom traces on sync + upload paths | `data/observability/FirebasePerformanceTracker.kt` |
| 8 | **Remote Config** | Feature flags with live updates | `data/config/FirebaseFeatureFlags.kt` |
| 9 | **Cloud Functions** | Scheduled per-note reminder delivery (Python) | `functions/main.py` |
| 10 | **App Check** | Attestation: only the genuine app reaches the backend | `FirebaseInActionApp.onCreate()` |

---

## 1. Authentication
Email/password and Google Sign-In (Credential Manager), with an email-verification gate before the
main app. The session persists in DataStore; sign-out clears session, local notification cache,
Crashlytics user, and Analytics identity in one place. Sign-in/up also captures the FCM push token —
and, because a persisted session never re-runs sign-in, the token is **re-synced on every app
startup** for returning users.
**Lesson:** the SDK silently refreshes ID tokens (~1h); `AuthStateListener` going null means sign-out,
revocation, or deletion — and deletions only surface on the next refresh unless you `reload()` at
startup. ([deep dive](firebase-auth-token-refresh-and-session-lifetime.md))

## 2. Cloud Firestore
Three data shapes, all user-scoped:
`users/{uid}/notes/{noteId}` (notes) · `users/{uid}/notifications/{id}` (inbox, **server-authored
only**) · `fcmToken` field on `users/{uid}`. Offline-first: Room is the read cache, edits mark rows
unsynced, `NoteSyncWorker` (WorkManager) pushes, snapshot listeners pull. Rules are hardened —
owner-scoped, email-verified for notes, `create: if false` for client notification writes,
mark-read-only updates. One composite **collection-group index** (`reminderFiredAt ASC, reminderAt
ASC`) serves the reminder scan.
**Lesson:** rules match paths *independently* — a subcollection rule grants nothing on its parent
doc. The missing `users/{uid}` rule silently blocked every token write for months.

## 3. Cloud Storage
Note cover images at `users/{uid}/notes/{noteId}/cover.jpg`: byte-level upload progress surfaced to
the UI, download URL saved onto the note, WorkManager retry on network failure, and a performance
trace around every upload.
**Lesson:** uploads are long-lived operations — model them as a progress *flow* (Progress/Success/
Error), not a one-shot call, and plan the cancel/orphan-cleanup path from day one.

## 4. Cloud Messaging (FCM)
The product with the deepest mechanics. Notey sends **data-only** messages (from the Cloud Function),
because they *always* invoke `onMessageReceived` — foreground or background — keeping display fully
under app control: in-app banner via `InAppNotificationBus` when foregrounded, system notification
(own channel, deep-link PendingIntent) when not. Notification-payload messages (e.g. Console
campaigns) are handled too (`FcmPayloadKind.DISPLAY`): correct content in the foreground, and the
manifest's `default_notification_channel_id` keeps the background auto-display in our channel. Deep
links `notey://notification?notificationId=` and `notey://note?noteId=` are registered in three
places that must agree: constants, `navDeepLink`, manifest intent-filter.
**Lesson:** a message with a `notification` payload **never reaches your code** while backgrounded —
that single SDK behavior dictates the entire data-only architecture.

## 5. Crashlytics
Release builds plant `CrashReportingTree`: every WARN/ERROR Timber log becomes a Crashlytics
breadcrumb, exceptions are recorded, and the user (uid, `is_email_verified`) is tagged at sign-in
*and re-tagged at startup* for persisted sessions. Disabled entirely under the emulator flavor.
**Lesson:** route crash context through the logging abstraction you already have (Timber) instead of
sprinkling `Crashlytics.log()` calls — one tree, whole-app coverage.

## 6. Analytics
A domain-level `AnalyticsTracker` interface with a **composite** implementation fanning events out to
Firebase Analytics *and* Mixpanel simultaneously (`SignIn/SignUpCompleted`, `NoteCreateStarted`,
`NoteSaved`, `NoteImageAdded`, `NoteDeleted`; `identify`/`reset` on auth changes). Emulator builds
opt out to keep dev noise out of production data.
**Lesson:** never call a vendor SDK from a ViewModel — the composite-behind-an-interface pattern made
adding Mixpanel a one-file change.

## 7. Performance Monitoring
Custom traces where latency actually matters: `note_sync` (WorkManager push), `image_upload`,
`firestore_fetch_notes` — each with attributes/metrics (unsynced counts, file sizes, error states)
and explicit stop on success, failure, *and* cancellation. A `NoOpPerformanceTracker` swaps in under
the emulator.
**Lesson:** the automatic traces are table stakes; the value is custom traces around *your* critical
paths, and remembering that an un-stopped trace on the cancel path silently reports garbage.

## 8. Remote Config
A typed `FeatureFlags` abstraction (`StateFlow<AppConfig>`) over the SDK: three flags —
`enable_in_app_banner` (FCM banner kill-switch), `max_free_notes` (creation gate),
`welcome_message` (live Home banner) — with real-time updates via a `callbackFlow`-wrapped
config-update listener. The template is config-as-code (`remote_config.json`, file = source of
truth, `deploy --only remoteconfig`).
**Lesson:** the SDK already persists activated values on disk — adding your own cache is an
antipattern; and `deploy` is a *full-template replace*, so pick one source of truth.
([deep dive](firebase_remote_config.md))

## 9. Cloud Functions
Python 3.12, 2nd gen: `check_due_reminders` runs every minute (Cloud Scheduler), collection-group
scans for due-and-unfired note reminders, and delivers each through a reusable
`deliver_notification()` seam — Firestore inbox doc + data-only FCM push, idempotent via
deterministic `{noteId}_{reminderAt}` ids, marked fired *before* sending so failures can't retry
forever. Built and tested emulator-first; Blaze only needed at deploy.
**Lesson:** the sender authors the inbox (client only displays) — one writer, no echo loops; and
Pub/Sub redelivery *will* happen, so idempotency is a requirement, not polish.
([deep dive](firebase_cloud_functions.md))

## 10. App Check
Installed as the *first* statement in `onCreate()` — Debug provider on debug builds (per-install
secret, console allow-list), Play Integrity on release — with enforcement enabled on Firestore and
Storage. Proven both ways on a real device: attested app unaffected; after a `pm clear` (fresh
unregistered token), a **fully authenticated user was rejected on every Firestore call**.
**Lesson:** authenticated ≠ attested. Rules ask "is this user allowed?"; App Check asks "is this even
my app?" — and attestation is evaluated first. ([deep dive](firebase_app_check.md))

---

## Supporting infrastructure

### Emulator Suite (dev flavor)
The `dev` flavor (`USE_EMULATOR=true`, applicationId `.dev`) points Auth/Firestore/Storage at local
emulators via `core/emulator/FirebaseEmulatorConfig.kt`; `prod` talks to the live project.

| Emulator | Port | | Emulator | Port |
| --- | --- | --- | --- | --- |
| Auth | 9099 | | Functions | 5001 |
| Firestore | 8080 | | Pub/Sub | 8085 |
| Storage | 9199 | | UI | 4000 |

**No emulator exists for Remote Config or App Check** — both always hit the live backend, from every
flavor. Crashlytics/Analytics/Performance are disabled or no-op'd under `USE_EMULATOR` instead.

### Architecture conventions
Clean architecture (`domain` interfaces → `data` implementations → `presentation` MVI ViewModels),
Koin DI, one Koin module per feature aggregated in `core/di/AppModule.kt`. Every Firebase SDK hides
behind a domain interface (`AuthRepository`, `NoteRepository`, `FeatureFlags`, `AnalyticsTracker`,
`CrashReporter`, `PerformanceTracker`, `NotificationDisplayer`) — the presentation layer never
imports Firebase. Cross-cutting behavior gates on `BuildConfig` (`USE_EMULATOR`, `DEBUG`).

### Config-as-code inventory
Everything backend-shaped is versioned: `firebase.json` (services + emulators),
`firestore.rules`, `storage.rules`, `firestore.indexes.json`, `remote_config.json`,
`functions/` (source + `requirements.txt`). Deploys are always scoped (`--only <product>`) — a bare
`firebase deploy` would push *everything*, clobbering manual backend edits.

---

## The integration showcase: a reminder's journey

One feature that threads six products end to end:

1. **UI** — user picks a date+time on a note (Material 3 pickers, `reminderAt` into state).
2. **Room → Firestore** — the note syncs like any edit (`NoteSyncWorker`). *(Firestore)*
3. **Scheduled function** — the every-minute scan finds it due, writes the inbox doc, sends a
   **data-only push** to the user's `fcmToken`, stamps `reminderFiredAt`. *(Cloud Functions)*
4. **App Check** — the client's original writes were attested; the function's Admin SDK writes
   bypass attestation by design. *(App Check)*
5. **Device** — `onMessageReceived` fires (foreground *or* background), shows banner or tray
   notification. *(FCM)*
6. **Tap** — `notey://note?noteId=…` deep-links straight to the note, auth-guarded. *(Auth + Navigation)*
7. **Meanwhile** — the sync listener pulls the inbox entry into Room; the unread badge updates;
   upload/sync paths were traced. *(Performance)*

Three real production bugs were found and fixed while verifying this pipeline live — the missing
parent-doc rule, the never-resynced push token, and a log-filter red herring — all documented in the
[Cloud Functions notes](firebase_cloud_functions.md).

## Not covered (deliberately)

| Product | Status |
| --- | --- |
| **AI Logic (Gemini)** | Deferred until needed — costs understood (per-token, free tier via the Developer API; $1 billing alert in place) |
| **Data Connect (SQL)** | Untouched — would contrast relational vs the Firestore model |
| **Hosting / App Hosting** | No web surface yet |
| **In-App Messaging, A/B Testing** | Natural extensions of the RC + Analytics foundation already in place |
