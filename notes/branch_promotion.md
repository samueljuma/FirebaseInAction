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

## If "Create a merge commit" is missing from the merge button

Two independent switches can hide it (both hit during the first real promotion, 2026-07-03):

1. **Repo setting** — Settings → General → *Pull Requests* → **"Allow merge commits"** must be
   ticked. Unticked, the option vanishes from every PR in the repo.
2. **"Require linear history"** on the *target* branch (ruleset or classic protection) — this rule
   *forbids* merge commits, so GitHub hides the option even when the repo setting and the ruleset's
   "allowed merge methods" both permit them. **Linear history is fundamentally incompatible with a
   promotion workflow** — a promotion *is* a merge commit. Untick it for `staging`/`main`; `dev`
   stays effectively linear anyway (squash-only features), and release-branch history reads as a
   clean series of promotion merges.

Check what's actually in force on a branch (the effective union of all rules):
```bash
gh api repos/<owner>/<repo>/rules/branches/staging --jq '.[].type'
# look for: required_linear_history
```

## If merging is blocked: "This branch is out-of-date with the base branch"

Hit on the *second* promotion (PR #19): checks green, but merging blocked with an offer to
"merge the latest changes from staging into this branch."

**Cause:** each promotion's merge commit lives only on the *target* branch — `dev` never gets it, so
after the first promotion `dev` is permanently "1 commit behind" `staging` (ancestry-wise; content is
identical). The ruleset option **"Require branches to be up to date before merging"** (the strict
sub-option of required status checks) demands the head contain every commit of the base → every
promotion after the first is blocked. Inherent collision between the merge-commit promotion model
and strict up-to-date checking.

**Fix options:**
- **Untick "Require branches to be up to date before merging"** in the ruleset (what we did). For
  promotion PRs the strictness adds zero information — the target only ever receives what the head
  sent it — and CI also runs on *push* to all three branches, so post-merge breakage is still caught.
  Refinement if wanted: two rulesets — strict on `dev` (guards racing feature PRs), non-strict on
  `staging`/`main`.
- Or click GitHub's **"Update branch"** button each time: back-merges the target into the head
  (content-harmless, but pure ceremony — an identical tree gets re-tested, and the head's history
  braids with a back-merge commit *every* promotion).

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
Merge with **Create a merge commit**. Then optionally tag/release — see
[Tagging & releases](#tagging--releases) below.

> **First promotion (done 2026-07-03):** `staging` and `main` had sat at the initial commit since
> project setup; PRs #16 (`dev → staging`) and #17 (`staging → main`) carried all 15 accumulated
> commits through, both as merge commits. Verified afterwards: zero commits behind in the promotion
> direction and identical trees on `dev` and `main`.

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

## Tagging & releases

After `staging → main` merges, mark the released state. **No checkout needed either way** — a tag
points at a *commit*, not a branch.

### Path A — one command via `gh` (tag + GitHub Release together)
```bash
gh release create v1.x.0 --target main \
  --title "v1.x.0" \
  --notes "What shipped."
```
- Creates the tag server-side at `--target` if it doesn't exist. **Always pass `--target`
  explicitly** — when omitted, a new tag lands on the repo's *default branch* tip, which on
  dev-defaulted repos silently tags the wrong branch.
- Your local clone won't see the tag until the next `git fetch`.
- The tag is *lightweight*; the Release object carries the metadata (title, notes, date) instead.

### Path B — plain git (annotated tag, no Release object)
```bash
git fetch origin                              # so origin/main points at the fresh merge
git log --oneline -1 origin/main              # sanity: tagging the right commit?
git tag -a v1.x.0 origin/main -m "v1.x.0"     # -a = annotated (tagger/date/message in git itself)
git push origin v1.x.0                        # tags don't travel with git push
```
- `origin/main` after the tag name is the *target* — omit it and git tags **HEAD**, i.e. whatever
  you have checked out (the classic way `dev` gets tagged by accident).
- Use this path when the annotation should live in git history (`git show v1.x.0`) independent of
  GitHub; wrap a Release around an existing tag later with `gh release create v1.x.0` if wanted.

> **v1.0.0 (2026-07-03):** created via Path A, targeting `main` at the second promotion's merge
> commit — the ten-Firebase-products state.

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
