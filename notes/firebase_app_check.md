# Firebase App Check (Play Integrity)

Attests that requests to your Firebase backends come from **your genuine app** — not a stolen API key,
a curl script, or a repackaged APK. The `google-services.json` config is not a secret (it ships in
every APK); without App Check, anyone holding it can talk to your project's endpoints directly,
limited only by Security Rules.

## Attestation vs. authorization — two independent questions

| Layer | Question it answers | Enforced by |
|---|---|---|
| Security Rules | *Is this **user** allowed to touch this data?* | `firestore.rules` / `storage.rules` |
| App Check | *Is this even **my app** making the call?* | Service-level enforcement toggle |

They're complementary, not overlapping: rules can't tell a genuine app from a script running with a
stolen user token; App Check can't tell whether the authenticated user owns the document. The recent
rules hardening (server-only notification creates, owner-scoped `users/{uid}`) closed the
*authorization* gaps — App Check closes the *provenance* one.

## How a request gets attested
1. On app start, the installed **provider** proves the app's integrity to the App Check backend and
   receives a short-lived **App Check token**.
2. The Firebase SDKs automatically attach that token to every request to enforced services.
3. An enforced service rejects requests without a valid token — before rules even run.

## Providers, by build type

| Build | Provider | How it attests |
|---|---|---|
| `debug` (devDebug/prodDebug) | **Debug provider** | Prints a per-install secret to logcat; you allow-list it in the console. No device integrity involved — it's a developer bypass. |
| `release` | **Play Integrity** | Google Play's device/app integrity verdict — genuine APK, untampered device. |

---

## Milestone 0 — Client wiring

### Where the install goes: literally first in `onCreate()`
`FirebaseAppCheck.getInstance().installAppCheckProviderFactory(...)` runs **before anything else** in
`FirebaseInActionApp.onCreate()` — before `FirebaseEmulatorConfig.configure()` (which touches
Firebase.auth/firestore/storage), before Koin builds repositories, before the FCM token resync. The
provider must be installed before the first Firebase request, or that request goes out unattested.
(`FirebaseApp` itself auto-initializes via ContentProvider *before* `onCreate` — that's fine; what
matters is beating the first token-consuming call, and nothing fires one earlier than our own code.)

```kotlin
FirebaseAppCheck.getInstance().apply {
    installAppCheckProviderFactory(
        if (BuildConfig.DEBUG) DebugAppCheckProviderFactory.getInstance()
        else PlayIntegrityAppCheckProviderFactory.getInstance()
    )
    setTokenAutoRefreshEnabled(true)
}
```

### `setTokenAutoRefreshEnabled(true)` — why explicit
Left unset, App Check token auto-refresh **follows the Analytics collection setting** — which this
project deliberately disables under `USE_EMULATOR`. Without the explicit call, dev-flavor builds would
silently stop refreshing App Check tokens. A classic "two unrelated toggles secretly coupled" trap.

### Why the debug provider is a plain `implementation` dependency
The runtime `if (BuildConfig.DEBUG)` gate references `DebugAppCheckProviderFactory` from main sources,
so the class must exist on the **release** compile classpath — `debugImplementation` would fail the
release build. Trade-off accepted: the class ships in release but is never installed, and a debug
provider is useless without a console-registered token anyway. (The "clean" alternative — a
debug/release source-set split with a small installer interface — buys purity at the cost of
indirection; not worth it here.) Kept as a **separate catalog entry, not in the `firebase` bundle**, so
the bundle stays "things every build actually uses."

### Verify
- `compileDevDebugKotlin` clean; both flavors build and launch with no crash.
- Logcat (`DebugAppCheckProvider`) prints the per-install debug secret on both targets:
  - Physical device (prodDebug → prod app)
  - AVD (devDebug → `.dev` app)
- Nothing is enforced yet, so the app behaves identically — the tokens minted here get registered in
  the console in M1.

---

## Milestone 1 — Console registration

Console-only milestone (no code). Three registrations, in order:

1. **SHA-256 fingerprints** (Project settings → General, per app): both the debug keystore's and the
   release keystore's SHA-256, on *both* Android apps. Pulled via `./gradlew :app:signingReport` —
   the debug keystore is shared by all debug builds; the release keystore covers release builds.
2. **Play Integrity provider** (App Check → Apps tab): registered on both apps → status "Registered".
3. **Debug tokens** (⋮ → Manage debug tokens, per app): each install's logcat-minted secret,
   registered under its **matching** app — physical device's token under the prod app, AVD's under
   the `.dev` app. Cross-registering silently fails.

### Where debug tokens come from (common confusion)
The Debug provider mints a **random per-install UUID on first run** and prints it to logcat:
```
DebugAppCheckProvider: Enter this debug secret into the allow list in the Firebase Console...
```
The app invents the secret; the console registration says "trust this one install." Clearing app
data, reinstalling, or recreating the AVD mints a **new** secret that must be re-registered — a
recurring dev-loop annoyance to expect, not a bug.

### Verification, and what "success" looks like
After registration, relaunched both builds and checked logcat:
- **Physical (prodDebug): silence.** No token-exchange errors — the SDK doesn't log successful
  exchanges, so *absence* of the earlier failure spam is the success signal.
- **AVD (devDebug):** `Unable to resolve host "firebaseappcheck.googleapis.com"` — the emulator's
  internet/DNS was down (AVD flakiness; cold reboot fixes it), which surfaced two lessons:
  - The Debug provider **always needs the live App Check backend**, even when Firestore/Auth point
    at local emulators — there is no App Check emulator.
  - When it can't reach it, the SDK degrades to a **placeholder token** and requests still flow.
    Harmless for the dev flavor: the local emulators never enforce App Check anyway.

### Console UI drift
The console's App Check screens had changed from the steps as originally written (flows re-skinned,
same shape: Apps tab → app → attestation provider → Registered). Console instructions age fast —
navigate by concept (register provider, manage debug tokens, enforce per-service), not by pixel.

---

## Milestone 2 — Enforcement, proven both ways

Enforcement enabled in the console for **Cloud Firestore and Cloud Storage only** (instantly
reversible toggles). Not Functions — the only function is *scheduled*, not callable/HTTP, so App Check
never applies to it; its Admin SDK writes bypass App Check (and rules) entirely, by design. Not
Auth/RC — scope kept tight. Same-day enforcement was fine for a single-dev app: monitoring mode exists
to discover unknown legitimate clients, and there were none. (Console metrics lag hours — verify by
functional testing, not dashboards.)

### Positive proof — attested app under enforcement
On the physical device (prodDebug, registered token): startup alone exercises the enforced services —
`Remote sync: processed 7 remote notes`, `Synced 2 notifications`, zero errors — and a note cover
image upload succeeded (Storage path). Business as usual, because the install is attested.

### Negative proof — the payoff demo
The elegant realization: **no un-registering needed.** Debug tokens are per-install, so
`pm clear` alone mints a brand-new, unregistered token — the device becomes an "unknown app" in one
step. Then, after signing back in (Auth is *not* enforced, so sign-in succeeds):

```
Firestore: Write failed at users/<uid>: PERMISSION_DENIED
NoteRepository: Firestore listener error ... PERMISSION_DENIED
NotificationRepo: Firestore listener error ... PERMISSION_DENIED
```

The sharpest teaching moment of the track: a **fully authenticated user**, making requests that pass
the security rules (owner-scoped, verified earlier the same day), rejected on every Firestore surface —
purely because the app *install* isn't attested. Attestation is evaluated **before** rules ever run;
they're independent gates.

Registering the freshly-minted token in the console and relaunching restored everything instantly
(7 notes + 2 notifications syncing again, zero errors). Rejection → one console entry → recovery.

### What enforcement means for the rest of the stack
| Component | Under enforcement |
|---|---|
| prodDebug w/ registered token | Works (attested via Debug provider) |
| devDebug on AVD → local emulators | Unaffected — emulators never enforce App Check |
| Scheduled reminder function | Unaffected — Admin SDK bypasses App Check |
| FCM pushes | Unaffected — FCM isn't an enforced surface here |
| `prodRelease` sideloaded via adb | **Would fail** — Play Integrity only attests Play-distributed installs; truly testing the release path needs a Play internal-testing track |
| Stolen `google-services.json` + script | Blocked — the entire point |

### The one honest gap
The Play Integrity (`else`) branch of the provider install is effectively **untestable without Play
distribution**. Every build we can run locally is a debug build using the Debug provider. Documented
rather than solved: the day Notey ships to a Play track, the release path gets its first real
attestation — and if something's wrong (e.g. missing SHA-256), enforcement would brick release users,
which is why the console's per-service *metrics* (verified vs unverified breakdown) are worth a glance
before enforcing in a real production rollout.
