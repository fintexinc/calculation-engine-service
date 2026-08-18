---
description: "Preview & verify all CONTRIBUTING.md pre-PR steps, run the code-reviewer skill, optionally commit changes, then optionally open the PR"
allowed-tools: ["Bash", "Read", "Grep", "Glob", "AskUserQuestion", "Skill"]
---

# /pr-review — Pre-PR checklist gate

Verify every step from `CONTRIBUTING.md` that must be completed **before** opening a pull
request. This command runs read-only checks and reports status. Its only state-changing actions
are **optional and explicitly confirmed**: creating a commit (step 4) and opening the PR
(step 9). It never stages, commits, or pushes without your confirmation.

Run the steps below **in order**. For each step print one checklist line:

- ✅ **pass**
- ❌ **fail** — include the exact remediation command
- ⚠️ **needs attention** — manual/advisory item

Collect all results, then print the summary table and decide whether to offer PR creation.
Do **not** stop early on a failure — run every automated check so the developer sees the full
picture in one pass.

---

## 1. Formatting ✅/❌

```bash
mvn spotless:check
```

If it fails, report the unformatted files from the output and advise (do **NOT** run it
yourself):

```bash
mvn spotless:apply
```

## 2. Tests ✅/❌

```bash
mvn test
```

Report pass/fail. On failure, summarize the failing test class(es)/method(s) from the output.

## 3. Rebase status ✅/❌

```bash
git fetch origin
git rev-list --count HEAD..origin/main
```

If the count is `0` the branch is up to date with `origin/main` → ✅.

If `> 0`, the branch is behind → ❌. **The branch must be rebased before preparing any commit
(step 4)** — always check this first. Do not merely advise the command: when the branch is behind,
**ask the developer for confirmation before starting the rebase** (never rebase automatically),
then run it:

```bash
git rebase origin/main
```

Only proceed to step 4 (commit prep) once the rebase completes (or the developer declines). If the
rebase produces conflicts, stop and report them — do not auto-resolve.

## 4. Commit changes (optional, confirmed) ✅/⚠️

**Prerequisite:** step 3 must be green first. If the branch is behind `origin/main`, do not prepare
or create any commit until the rebase is done — ask the developer to confirm the rebase, run it,
and only then continue here.

Detect whether there is uncommitted work:

```bash
git status --porcelain
```

- If the output is **empty**, the working tree is clean → ✅ (nothing to commit), skip to step 5.
- If there are modified, staged, or untracked changes, **offer to create a commit**. You MUST ask
  the developer before staging or committing anything — never commit automatically.

When offering to commit:

1. Show the developer what changed so they can decide what to include:

   ```bash
   git status
   git diff --stat HEAD
   ```

2. **Propose a commit message that conforms to the `Commit Message Guidelines` section of
   `CONTRIBUTING.md`:**
   - Conventional Commits subject: `<type>(<scope>): <description>` where `type` is one of
     `feat | fix | docs | style | refactor | perf | test | chore | ci`.
   - A short `[optional body]` explaining **what & why** when the change is non-trivial.
   - A **ticket footer** is mandatory — `refs:` (general), `fixes:` (bug fix), or `closes:`
     (feature completion) followed by the ticket number. Derive the ticket from the current
     branch name (e.g. branch `TMI-454-...` → `refs: TMI-454`); prefer `fixes:` for bug fixes
     and `closes:` for completed features. Confirm the ticket with the developer if the branch
     name has no ticket.

3. Use `AskUserQuestion` (or a plain prompt) to confirm **(a)** which changes to stage — all of
   them or a subset — and **(b)** the final commit message. Call out anything that looks
   unrelated to the ticket (e.g. stray `.gitignore` edits) so it isn't committed by accident.

4. **Always ask whether to amend the existing commit or create a new one** — never assume. Use
   `AskUserQuestion` with these options, showing the current branch tip (`git log --oneline -1`)
   so the developer knows what would be amended:
   - **Amend the existing commit** — folds the staged changes into `HEAD`, keeping (or letting the
     developer edit) its message. Prefer this to keep the branch at 1–2 commits (see step 5). Note
     it **rewrites history**, so a subsequent push needs `--force-with-lease`.
   - **Create a new commit** — adds a separate commit with the approved message.

   If the branch has no commits ahead of `origin/main` yet (`git rev-list --count origin/main..HEAD`
   is `0`), skip the question — there is nothing to amend, so create a new commit.

5. Only after explicit confirmation of **both** the changes to stage and the amend-vs-new choice,
   stage the agreed paths (not a blanket `git add -A` unless the developer chose "all"), then:

   ```bash
   # new commit
   git add <agreed paths>
   git commit -m "<type>(<scope>): <description>" -m "<body>" -m "<footer>"

   # OR amend the existing commit
   git add <agreed paths>
   git commit --amend -m "<type>(<scope>): <description>" -m "<body>" -m "<footer>"   # or --no-edit to keep the message
   ```

Mark this step ⚠️ if uncommitted changes remain after the developer declines to commit (they
must commit before a PR can be opened), otherwise ✅. **Never commit or amend without explicit
confirmation.** After a commit is created or amended, steps 5 and 6 below reflect the new state.

## 5. Commit count ✅/⚠️

```bash
git rev-list --count origin/main..HEAD
```

CONTRIBUTING.md requires **1–2 commits max**. If the count is `0`, mark ❌ — there is nothing to
open a PR from (go back to step 4 and commit). If the count is `> 2`, mark ⚠️ and advise an
interactive squash:

```bash
git rebase -i HEAD~N   # change 'pick' to 'squash'/'s' for commits to combine
```

## 6. Commit message conventions ✅/❌

```bash
git log origin/main..HEAD --format='%H%n%B%n---'
```

For each commit verify:

- Subject follows Conventional Commits: `<type>(<scope>): <description>` where `type` is one of
  `feat | fix | docs | style | refactor | perf | test | chore | ci`.
- A ticket footer is present: `refs:` / `fixes:` / `closes: TMI-xxx`.

Report any commit that does not conform. Remediation: `git commit --amend` (or squash) to fix
the message, then `git push --force-with-lease`.

## 7. Code review via `code-reviewer` skill (MANDATORY) ✅/❌

This step is **required**, not advisory. Invoke the project's `code-reviewer` skill on the full
diff of the branch and report its findings.

```bash
git diff origin/main...HEAD          # committed changes to review
git diff HEAD                        # include any still-uncommitted changes
```

Invoke the **`code-reviewer`** skill (via the `Skill` tool) on that diff and report the findings
grouped by severity — **Critical / High / Medium / Low** — each with `file:line` and the required
fix.

- ✅ **pass** — no **Critical** and no **High** findings remain.
- ❌ **fail** — one or more Critical/High findings. List them; the PR must **not** be opened until
  they are resolved. Medium/Low findings are reported but do not block (note them for the author).

Do not skip this step even if steps 1–6 passed — formatting and tests passing does not mean the
change is correct.

## 8. Quality gates (advisory) ⚠️

Additional manual reminders **not already covered by step 7** — flag them, do not attempt to run
them:

- [ ] PR description explains **what and why** (not how)

The architectural/resilience gates (N+1 external calls, Resilience4j on MIC calls, hardcoded
configuration) are checked by the `code-reviewer` skill in step 7.

---

## 9. Summary

Print the full CONTRIBUTING.md PR checklist with each item marked from the results above:

```
Pull Request Checklist
- [ ] Code is formatted (mvn spotless:apply)        → step 1
- [ ] All tests pass (mvn test)                     → step 2
- [ ] Branch is rebased onto latest main            → step 3
- [ ] All work is committed                         → step 4
- [ ] Commits follow Conventional Commits format    → step 6
- [ ] Commits include ticket references             → step 6
- [ ] PR contains 1-2 main commits                  → step 5
- [ ] code-reviewer skill run, no Critical/High     → step 7
- [ ] No N+1 external service calls                 → step 7
- [ ] Resilience4j annotations on MIC calls          → step 7
- [ ] No hardcoded configuration values             → step 7
- [ ] PR description explains what and why (not how) → step 8 (manual)
```

## 10. Offer to open the PR

**Only if steps 1, 2, 3, 5, 6, and 7 all pass** (formatting, tests, rebase, commit count, commit
messages, and code review with no Critical/High findings), offer to create the PR. If any of those
failed, do **not** offer — tell the developer to fix the ❌ items first.

When offering:

1. Propose a title and body generated from the commit messages (the "what & why").
2. Ask the developer to confirm before running anything.
3. Only after explicit confirmation, run:

```bash
gh pr create --base main --title "<title>" --body "<body>"
```

Never run `gh pr create` without explicit confirmation.
