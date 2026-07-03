# Contributing to FirebaseInAction

## Branching strategy

| Branch | Purpose |
|---|---|
| `main` | Production. Every merge here is releasable and gets tagged (`vX.Y.Z`). |
| `staging` | Pre-production / QA. Mirrors what's about to ship. |
| `dev` | Integration branch. All feature work lands here first. |
| `feature/*`, `fix/*`, `chore/*` | Branches cut from `dev`, one per unit of work. Kept after merge (not auto-deleted) for reference. |
| `hotfix/*` | Cut from `main` for urgent production fixes. |

`main`, `staging`, and `dev` are protected: no direct pushes, PRs only, 1 approval required, CI must pass.

## Workflow

1. Branch off `dev`:
   ```
   git checkout -b feature/short-description dev
   ```
2. Commit using [Conventional Commits](https://www.conventionalcommits.org/): `type(scope): summary`
   (`feat`, `fix`, `chore`, `refactor`, `docs`, `test`) — e.g. `feat(fcm): add deeplinking for notifications`.
3. Open a PR into `dev`.
4. CI must pass (build, unit tests, lint).
5. Get 1 approval before merging. On a solo repo that can be a self-review — read your own diff top to bottom before approving, don't rubber-stamp it.
6. Squash-merge into `dev`. The branch is kept afterward, not deleted.

## Promoting changes

```
dev     → staging   PR, once a batch of features is ready for QA
staging → main      PR, once verified in staging
```

Use a regular merge (not squash) for these promotion PRs, so the individual feature commits already squashed into `dev` stay intact. Tag a release (`vX.Y.Z`) on `main` after each promotion.

## Hotfixes

Branch `hotfix/*` from `main`, PR back into `main`, then immediately back-merge `main` into `staging` and `dev` so they don't drift out of sync.
