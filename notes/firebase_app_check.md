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
