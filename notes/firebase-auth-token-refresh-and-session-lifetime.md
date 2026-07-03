# Firebase Auth: Token Refresh and Session Lifetime

## Two Token Layers

| Token | Lifetime | Purpose |
|---|---|---|
| **ID Token** | ~1 hour | Proves identity to backends |
| **Refresh Token** | Indefinite (until revoked) | Silently mints new ID tokens |

## What Happens at the 1-Hour Mark

The Firebase SDK silently refreshes the ID token in the background using the refresh token. The user stays logged in. `AuthStateListener` may fire again, but `currentUser` is still non-null — the app continues working with no user-visible interruption.

## When `AuthStateListener` Actually Fires with `null`

- Explicit `signOut()` call
- Refresh token revoked (via Firebase Admin SDK or console)
- Token refresh fails with `FirebaseAuthInvalidUserException` — account was deleted or disabled

## The Deletion Gap

Deleting a user from the Firebase console does **not** notify the device. The SDK only discovers it when it next attempts a token refresh and gets `FirebaseAuthInvalidUserException` back. Until then (up to ~1 hour), the user appears logged in locally.

**Fix:** Call `currentUser.reload()` on startup — a lightweight server round-trip that immediately validates the account still exists, rather than waiting for the next refresh cycle.
