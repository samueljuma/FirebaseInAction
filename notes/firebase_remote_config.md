# Firebase Remote Config

Change app behaviour and appearance **without shipping an update**, by reading a cloud-hosted
config template at runtime. Built into the Notey app as a `FeatureFlags` layer, milestone by
milestone.

## Parameters we use

| Key | Type | Default | Drives |
|---|---|---|---|
| `enable_in_app_banner` | Boolean | `true` | Master switch for the foreground FCM banner |
| `max_free_notes` | Number | `50` | Note-count limit before the create action is blocked |
| `welcome_message` | String | `""` (blank hides it) | Optional greeting on the Home top bar |

---

## Milestone 0 — Provisioning as config-as-code

### The template is one document
A Remote Config project is a single JSON **template**: `parameters`, `conditions`,
`parameterGroups`, and a server-managed `version`. The CLI reads/writes that whole document.

### The clobber gotcha (why direction matters)

| Command | Direction | Effect |
|---|---|---|
| `remoteconfig:get -o remote_config.json` | cloud → local | **Overwrites the local file** with live state |
| `deploy --only remoteconfig` | local → cloud | **Replaces the entire cloud template** (not a merge) |

Because `deploy` is a *full replace*, a local file missing a param/condition would **delete** it in
the cloud. And because `get` overwrites local, editing *both* the console and the file causes drift —
whichever you run last wins, the other side's edits are lost.

**Rule:** pick **one** source of truth.

### Deploy is product-scoped (it does NOT touch Firestore)
`deploy --only remoteconfig` publishes *only* the RC template. Firestore rules, Storage rules,
indexes, and Auth on the backend are untouched. The clobber risk above lives entirely **inside** the
RC template — it never reaches other products.

> ⚠️ A bare `firebase deploy` (no `--only`) is different: it would also push local `firestore.rules`,
> `firestore.indexes.json`, and `storage.rules`, overwriting any manual remote edits to those.
> **Always scope RC pushes:** `deploy --only remoteconfig`.

### Our choice: file = source of truth
We checked the remote template first (`remoteconfig:get` into a scratch file) and it was **empty
(`{}`)** — nothing to clobber — so we made `remote_config.json` authoritative and pushed it:

1. Define params in `remote_config.json` (`firebase.json` points at it via `"remoteconfig"`).
2. `npx -y firebase-tools@latest deploy --only remoteconfig`.
3. `npx -y firebase-tools@latest remoteconfig:versions:list` to confirm / see rollback points.

From here we edit the **file**, not the console. (`version` is server-managed, so it's kept out of
the file.) If we ever need to reconcile a stray console edit, pull it back with `remoteconfig:get`.

**Result:** version 1 published — `enable_in_app_banner`, `max_free_notes`, `welcome_message` are live.

### Handling an out-of-band console edit
It happened: a `welcome_message` value was changed on the console *after* we chose file-as-truth.
The safe reflex before overwriting anything is to **pull the live value into a scratch file and diff
it** against the local file:

```bash
npx -y firebase-tools@latest remoteconfig:get -o /tmp/rc_live.json   # read-only, into scratch
# compare /tmp/rc_live.json ⇄ remote_config.json
```

| Diff result | Action |
| --- | --- |
| Values **match** | Nothing to do — already in sync (this was our case) |
| Values **differ**, keep the file | Ignore console; `deploy --only remoteconfig` (file wins) |
| Values **differ**, keep the console text | `remoteconfig:get -o remote_config.json` to pull it in, then commit |

**Discipline:** edit the **file** → `deploy`. Console edits are transient under file-as-truth; they're
overwritten on the next deploy unless deliberately pulled back first.

---

## Milestone 1 — The domain contract (SDK-free)

Three files in `domain/config/`, **zero Firebase imports** — this is the seam the whole app is
allowed to touch.

| File | Role |
|---|---|
| `AppConfig.kt` | Typed value object of the flags + `AppConfig.Defaults` |
| `RemoteConfigKeys.kt` | The parameter-name constants (contract with the backend template) |
| `FeatureFlags.kt` | `interface`: `val config: StateFlow<AppConfig>` + `suspend fun sync()` |

### Why an interface, not "just call Firebase"?
**Dependency Inversion Principle:** high-level code (ViewModels) depends on the `FeatureFlags`
abstraction, not on `FirebaseRemoteConfig`. Benefits:
- The SDK becomes an implementation detail we can swap or **fake in tests** (`FakeFeatureFlags`).
- Presentation code never imports Firebase — the dependency arrow points *inward*, toward the domain.

### Why a typed `AppConfig` instead of `getBoolean("...")` everywhere?
- One place decodes raw keys → typed fields; callers read `config.inAppBannerEnabled`, not stringly-typed keys.
- `AppConfig.Defaults` is the **single source of truth** for baseline behaviour, so the app is fully
  functional *before the first network fetch* and if fetch ever fails.

### Why `StateFlow` + `suspend sync()`?
- `StateFlow<AppConfig>` = always-has-a-value, reactive: UI can observe and recompose on change.
- `sync()` is the imperative "go fetch fresh values" trigger, kept separate from *reading* them
  (Interface Segregation — readers don't get a fetch button they shouldn't press).

> At this milestone nothing reads these yet — it compiles, behaviour is unchanged. Next we implement
> the Firebase-backed version behind this interface.

---

## Milestone 2 — Firebase-backed impl + DI + startup warm-up

`FirebaseFeatureFlags` is the **only** class that imports the RC SDK. It's wired via
`remoteConfigModule` (added to `appModules`) and warmed up in `FirebaseInActionApp.onCreate()`.

### The loading strategy (activate cached → fetch fresh → live updates)
1. **`init`** applies `setConfigSettingsAsync` + `setDefaultsAsync`, then seeds the flow from the
   SDK's on-disk cache (`toAppConfig()`), and starts the live-update stream.
2. **`sync()`** (fired from `Application` on `APPLICATION_SCOPE`) does `fetchAndActivate()` in the
   background — ready this session, cached for next.
3. **Live updates** re-activate and re-emit on server-pushed changes.

### Q: `callbackFlow` or `runCatching`? — Both, for different shapes
| SDK interaction | Shape | Tool |
| --- | --- | --- |
| `fetchAndActivate()` in `sync()` | one-shot suspend | `runCatching` (a Flow would be over-engineering) |
| `addOnConfigUpdateListener` | stream of callbacks + a registration to remove | `callbackFlow` |

`callbackFlow` is the right wrapper for the listener precisely because of **`awaitClose { registration.remove() }`** — the listener is unregistered when the collecting scope dies. Both the
one-shot and the stream write into one `MutableStateFlow` sink that backs `config`.

### Q: Do we persist AppConfig (SharedPrefs/Room)? — No, the SDK already does
The RC SDK persists **activated** values on disk itself. `getBoolean/getLong/getString` return the
last-activated values across restarts; our in-memory `AppConfig` is rebuilt each launch *from that
cache* in `init`. Adding our own store would just duplicate the SDK's — an anti-pattern.

| Tier | Source | Persisted | Returned by `getX()` when |
| --- | --- | --- | --- |
| Defaults | `setDefaultsAsync` (`AppConfig.Defaults`) | in-app | key never activated / first run / offline |
| Fetched | `fetch()` | on disk | staged, not yet live |
| **Activated** | `activate()` | **on disk** | present — **this is what we read** |

**Takeaway:** the config effectively survives restarts via the SDK cache; `Defaults` is only the
cold-start / fetch-failed fallback. No DB required.

### `minimumFetchInterval` & the emulator
RC has **no local emulator** — it always hits the live backend. So the interval is `0` in debug
(instant iteration) and `3600s` in release (avoid throttling / respect quotas).

### Verify at runtime
Run the app and watch Logcat (tag `FeatureFlags`): a `Synced (...)` line prints the resolved
`AppConfig` on launch. Change a value in the console (or edit `remote_config.json` + `deploy`) → a
`Live update → ...` line appears within seconds, no restart.

---

## Milestone 3 — Boolean flag → real feature (FCM banner kill-switch)

The payoff: `enable_in_app_banner` now controls an existing feature from the cloud, no release
required. In `MainViewModel.observeInAppNotifications()` we consult the flag before showing the
foreground banner:

```kotlin
InAppNotificationBus.events
    .filter { featureFlags.config.value.inAppBannerEnabled }   // remote kill-switch
    .onEach { notification -> /* …show banner… */ }
    .launchIn(viewModelScope)
```

### Design choices
- **Declarative gate via `filter`**, not an `if (...) return@collect` inside `collect`. Reading
  `config.value` inside `filter` gates the stream on current state *without* letting that state drive
  emissions.
- **Why not `combine(events, config)`?** It would re-emit whenever the flag flips and re-show stale
  banners — wrong. We want the flag to *gate*, not *trigger*.
- **Emit-time semantics:** the flag is evaluated per event, so a live flip suppresses the *next*
  banner; one already on screen (auto-dismisses in 8s) rides out. Right altitude for a transient UI
  element — no need for `flatMapLatest`-style reactivity that would entangle with manual dismissal.
- **Gate in the ViewModel**, not in `AppFirebaseMessagingService` — the messaging service stays a
  dumb emitter; presentation policy lives in presentation.
- **No DI change**: `MainViewModel` is `viewModelOf(::MainViewModel)`, so Koin auto-wires the new
  `FeatureFlags` constructor arg by type.

### Verify
Send an FCM test message with the app foregrounded → banner shows. Set `enable_in_app_banner=false`
(console or `remote_config.json` + `deploy`), send again → no banner (Logcat: "disabled via Remote
Config — skipping"). Flip back to `true` → banner returns. All without rebuilding the app.
