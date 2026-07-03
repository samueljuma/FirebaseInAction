# Branch Promotion Playbook

How code moves through this repo's three long-lived branches:

```
feature/* ──squash──▶ dev ──merge──▶ staging ──merge──▶ main
hotfix/*  ──squash──▶ main ──merge──▶ staging ──merge──▶ dev   (back-merge)
```

## The golden rule

**Squash *into* `dev`; merge-commit *between* long-lived branches. Never squash a promotion.**

Why: squashing rewrites the promoted commits into one brand-new SHA. `dev` and `staging` then no
longer share history — every future promotion PR re-lists commits that were already merged, GitHub
shows permanent "X commits behind" noise, and phantom conflicts appear. Merge commits keep the three
branches sharing the same underlying SHAs, so each promotion PR contains only what's genuinely new.

| PR direction | Merge method |
| --- | --- |
| `feature/*` → `dev` | **Squash and merge** (one clean commit per feature, `(#N)` suffix) |
| `dev` → `staging`, `staging` → `main` | **Create a merge commit** |
| `hotfix/*` → `main` | **Squash and merge** |
| back-merges (`main` → `staging` → `dev`) | **Create a merge commit** |

CI (`build` job) runs on PRs targeting all three branches, so every promotion gets checked. Pair
with required-status-check rules on the protected branches.

---

## Promoting a feature all the way to main

**1. Feature → dev** (the normal flow)
```bash
git checkout -b feature/thing dev
# ...work, commit...
git push -u origin feature/thing
gh pr create --base dev --head feature/thing --title "feat: thing"
```
Merge in the GitHub UI with **Squash and merge**; delete the feature branch.

**2. dev → staging** (promotion)
```bash
gh pr create --base staging --head dev --title "release: promote dev to staging"
```
Merge with **Create a merge commit** (GUI: the dropdown on the merge button). Do **not** delete
`dev` (GitHub sometimes offers — ignore).

**3. staging → main** (release)
```bash
gh pr create --base main --head staging --title "release: promote staging to main"
```
Merge with **Create a merge commit**. Optionally tag the release:
```bash
git fetch && git tag -a v1.x.0 origin/main -m "v1.x.0" && git push origin v1.x.0
```

> **First promotion note:** as of 2026-07-03, `staging` and `main` still sit at the initial commit —
> the first `dev → staging` PR will carry all 14 accumulated commits. That's expected; the steps are
> identical.

## Hotfix off main

**1. Fix on a branch off `main`, PR back into `main`:**
```bash
git checkout -b hotfix/broken-thing main
# ...fix, commit...
git push -u origin hotfix/broken-thing
gh pr create --base main --head hotfix/broken-thing --title "fix: broken thing"
```
Merge with **Squash and merge**; delete the hotfix branch. Tag if it's a release.

**2. Back-merge so every tier has the fix** (skip this and the fix gets clobbered — or conflicts —
on the next promotion):
```bash
gh pr create --base staging --head main --title "chore: back-merge main into staging"
# merge (merge commit), then:
gh pr create --base dev --head staging --title "chore: back-merge staging into dev"
```
Both with **Create a merge commit**. `dev` being ahead is fine — the merge only brings the hotfix.

## If a promotion PR has conflicts

Don't resolve by merging the base into `dev` (that pollutes `dev` with a backwards merge). Use a
throwaway promotion branch:
```bash
git checkout -b promote/dev-to-staging origin/dev
git merge origin/staging        # resolve conflicts here
git push -u origin promote/dev-to-staging
gh pr create --base staging --head promote/dev-to-staging --title "release: promote dev to staging"
```
Merge it (merge commit), delete the throwaway branch. If the golden rule is followed consistently,
this should rarely be needed.

## Keeping local in sync afterwards

```bash
git fetch origin
git checkout dev     && git merge --ff-only origin/dev
git checkout staging && git merge --ff-only origin/staging
git checkout main    && git merge --ff-only origin/main
```

---

## Appendix — why squashed promotions rot (the mechanics)

Git compares branches by **commit ancestry**, not by file content. Say `dev` has three squashed
feature commits:

```
init ── A ── B ── C        (dev)
init                        (staging)
```

**Squashing the promotion PR** builds a brand-new commit `S1` whose *content* equals A+B+C but whose
SHA is new and unrelated:

```
init ── A ── B ── C        (dev)
init ── S1                 (staging)      S1 ≠ A,B,C in git's eyes
```

The files now match on both branches — but A, B, C are still **not ancestors of `staging`**, and
three consequences compound from there:

1. **Phantom "behind" status, forever.** GitHub permanently shows `staging` as "3 commits behind,
   1 ahead" of `dev`, even though the content is identical. Only a merge can clear it.
2. **Every future promotion re-lists old commits.** When `dev` gains D and E, the next promotion PR
   shows **A, B, C, D, E** — git asks "which commits on `dev` aren't ancestors of `staging`?" and
   A/B/C still qualify. The PR gets noisier every cycle.
3. **The merge base rots — the source of real conflicts.** Three-way merge diffs both sides against
   their *common ancestor*, which with squashed promotions stays at `init` forever. The moment the
   two sides genuinely differ against that ancient base (a hotfix squashed onto `staging`, a
   conflict resolved once during a promotion), the same lines register as "changed on both sides"
   on **every subsequent promotion** — the same conflict, re-resolved for eternity.

**With a merge commit**, the promotion commit `M1` has `dev`'s tip as a parent:

```
init ── A ── B ── C ────────── (dev)
                   \
init ─────────────── M1        (staging)   A,B,C are now ancestors of staging
```

The merge base advances to `C`; the next promotion PR shows only D and E. No phantom-behind, no
re-resolved conflicts.

**Why squashing features *into* `dev` is still fine:** the feature branch is deleted immediately
after merging — divergence against a dead branch harms no one, and `dev` keeps a clean one-commit-
per-feature history. `dev`/`staging`/`main` never die, so preserved shared ancestry is what matters
between them.

**Also applies to "Rebase and merge":** it mints new SHAs too — same rot for promotions.
