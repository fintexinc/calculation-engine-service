---
name: review-lessons
description: >
  Use when writing or refactoring code, and when self-reviewing a diff before requesting review or
  opening a PR, in calculation-engine-service. A living checklist of concrete defects previously
  caught in code review, so they are not repeated. Triggers: "refactor", "rework", "self-review",
  "before PR", moving logic between methods/constructors, editing shared base classes, or changing
  existing tests.
---

# Review Lessons

Concrete defects that slipped past implementation and were caught in review, each distilled into a
preventive, **diff-checkable** rule. Scan this list before marking a change done and before
requesting review. Add a new entry every time a reviewer finds a real problem (protocol at bottom).

## Discipline — do not rationalize past these

### [process] A failing test may be a real bug, not an artifact
When an existing test starts failing after your change, **do NOT edit the test to make it pass**
until you have confirmed the production behavior it asserted was *intentionally* changed. A test
going red right after a refactor is the prime signal the refactor broke something.

Red flags — STOP and investigate the production code instead of the test:
- "This test is stale / just a test artifact — I'll update the mock/stub."
- Weakening an assertion, or stubbing away the exact input that triggers the failure.
- The failing input is an edge case (empty, null, zero, boundary) you did not consciously change.

> Example: an empty-input `NoSuchElementException` first surfaced as a failing service test, was
> dismissed as a mock artifact and stubbed to pass, and the underlying HTTP 500 regression shipped
> until a reviewer caught it.

## Technique checklist — verify against your diff

- [ ] **[correctness] Moved an operation earlier in the lifecycle** (into a constructor, eager field init, or an
  earlier call)? Re-verify every edge case the *later* position used to handle — especially empty
  collections and short-circuit gates that previously ran before it.
- [ ] **[null/empty-safety] `firstKey()`/`lastKey()`/`first()`/`last()` on a `NavigableMap`/`SortedMap`/`SortedSet`**
  that can be empty? Guard emptiness first — they throw `NoSuchElementException`.
- [ ] **[convention] Checking a collection or map for empty/null?** Use
  `org.springframework.util.CollectionUtils.isEmpty(...)` — never a bare `col.isEmpty()` or
  `col == null || col.isEmpty()`. It null-safely handles both `Collection` and `Map`. (project
  convention)
- [ ] **[contract/behavior] Refactoring near a documented graceful-degradation contract** (empty/insufficient data →
  `null` + warning, not an exception)? Preserve it, and keep/add a test pinning the
  empty/insufficient path.
- [ ] **[correctness] Changed the `%s` count in an `ErrorCode` message pattern?** `getFormattedMessage` calls
  `String.format`, so every existing `toException`/`toValidationException`/`asNotification` call site must be updated
  in the same diff — a stale call site throws `MissingFormatArgumentException` at runtime and turns a documented 4xx
  into an unhandled 500. Grep the enum constant for **all** callers, and expect message-text assertions to go red.
- [ ] **[correctness] Tagging an outcome from an exception type at an observability boundary?** Verify the exception
  can still reach that boundary — a client that maps transport/HTTP exceptions to domain exceptions first makes the
  `instanceof` branch dead, and the tag then reports a fabricated constant (e.g. every failure as `500`).
- [ ] **[test-quality] Behavior-preserving refactor?** Add a test for the specific pre-existing behavior you intend
  to keep, so a regression turns a test red instead of shipping silently.
- [ ] **[test-quality] Does each assertion fail when the named behavior is broken?** Trace the exercised path and
  exclude incidental causes such as deserialization filters, empty defaults, or earlier short-circuits that can make
  the expected value appear without reaching the behavior under test.
- [ ] **[test-quality] Asserting an error outcome — a thrown domain exception (unit) OR an HTTP error response
  (e2e `ErrorResponse`/`Notification`)?** Assert the full payload — the error code **and** the
  formatted message **and** the metadata/`param-N` map — not just the code, so a regression that
  drops a substituted value is caught. Apply this to *every* negative scenario at both layers (unit
  and HTTP boundary), not only the happy path. Anchor `now`/time-dependent expected values in one
  shared field so produced and asserted values cannot drift.
- [ ] **[test-quality] Choosing unit-test input?** Use representative, non-trivial input — not a single-element,
  constant-value happy path. Provide enough elements and month-to-month variation (multiple items,
  non-constant/negative values, boundary cases) to actually exercise the logic, rather than a
  degenerate constant repeated N times.
- [ ] **[test-quality] Positive-scenario test (a successful result)?** Assert the *complete* payload with exact
  values — every returned period/value, the result-set size, the performance window, and that
  warnings are empty — not just `isNotNull()` or a subset of periods. Derive expected values
  independently, or capture them once as golden from a trusted pipeline for e2e.
- [ ] **[test-quality] Both positive AND negative scenarios present in the executed suite (unit *and* e2e)?** Every
  test class must exercise at least one success path and at least one failure path for the behavior under test — never
  negative-only or positive-only. With the Template-Method abstract-base pattern the shared positive/negative may live
  in the base (e.g. `shouldReturnOk_whenSmsReturnsAvailableResponse` in `AbstractPortfolioCalculationE2ETest`) and be
  inherited — that counts, *provided the concrete class actually runs it*. When you add a concrete class that only
  introduces new negatives (or only a new positive), confirm the inherited suite still supplies the other side; if the
  class extends `Abstract*` directly with no shared positive, add one. Check the executed method list
  (surefire XML), not just the concrete `.java` file, since inherited tests are invisible in the source.

## Adding a new entry

When a reviewer finds a real problem, distill it (do not narrate the incident):
1. Pick the guidance **form**: **discipline** (a rule the author knows but rationalizes past under
   pressure → its own section with a prohibition + red-flag line) or **technique** (edge case / API
   foot-gun / contract / practice → a checklist item).
2. Tag it with an **issue type** from the vocabulary below; prefix the entry with `[type]`.
3. Write a one-line **rule** checkable against a diff, plus the **symptom** to look for.

Issue types:
- `correctness` — logic/edge-case bugs, wrong results, refactor-safety.
- `null/empty-safety` — NPEs, empty-collection foot-guns.
- `contract/behavior` — return/error contracts, graceful degradation, behavior preservation across changes.
- `convention` — project idioms & consistency (e.g. `CollectionUtils.isEmpty`, `BigDecimal.valueOf`, no magic strings).
- `test-quality` — assertion completeness, representative input, positive/negative/edge coverage.
- `process` — TDD/verification discipline (don't silence failing tests, evidence before claims).
- `resilience` — external-call timeouts, N+1, Resilience4j, fallback.
- `architecture` — hexagonal boundaries, abstraction/duplication, hierarchy.
- `performance` — algorithmic cost, unnecessary work.
- `configuration` — hardcoded config values.

Keep each entry to a few lines, generic, and reusable — no ticket numbers or incident/session
references. Prefer concrete API/edge-case rules over vague advice.

## Related
- **`clean-code`** — the general principles (SOLID, GoF, DRY/KISS/YAGNI, code smells & antipatterns) these concrete lessons are instances of
