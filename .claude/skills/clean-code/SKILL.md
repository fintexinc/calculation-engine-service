---
name: clean-code
description: Clean Code principles (SOLID, GoF Patterns, DRY, KISS, YAGNI), naming conventions, function design, and refactoring. Avoid antipatterns. Use when user says "clean this code", "refactor", "improve readability", or when reviewing code quality.
---

# Clean Code

Readable, maintainable code. This repo's concrete conventions live in **`coder`** (implementation) and CLAUDE.md; recurring review defects live in **`review-lessons`**. This skill is the general principles behind them — apply it, then defer to those for repo specifics.

## When to Use
- Triggered on: **write**, **fix**, **implement**, **realize**, **do**, **make**, **review** (as well as "clean this code" / "refactor" / "improve readability")
- Reducing complexity or improving naming during implementation or review

## Core Principles

| Principle | Meaning | Violation sign |
|-----------|---------|----------------|
| **DRY** | One unambiguous representation per piece of knowledge | Copy-pasted blocks |
| **KISS** | Simplest solution that works | Over-engineered, clever one-liners |
| **YAGNI** | Build only what's needed now | Config/abstraction "just in case", interface with one impl |
| **SOLID** | SRP, Open/Closed, Liskov, Interface Segregation, Dependency Inversion — here, DIP *is* the hexagonal port boundary; Open/Closed *is* add-a-metric-via-new-bean | God class, `switch`-on-type, application layer importing an adapter |
| **GoF patterns** | Proven solutions to recurring problems — this repo uses Strategy (`CalculationService` beans), Template Method (abstract service/calculation bases), Factory (enum `fromValue`), Builder, Observer (`Notification`) | Reinvented dispatch / duplicated skeleton; or a pattern applied with no smell to fix |

**DRY caveat:** incidental duplication that will evolve differently (e.g. `shippingCost` vs `insuranceCost`) is fine — don't force a shared abstraction prematurely. Extract only when the knowledge is genuinely the same.

## Naming

| Element | Convention | Example |
|---------|------------|---------|
| Class | PascalCase noun, specific responsibility | `SharpeRatioCalculation` (not `Manager`/`Helper`/`Utils`) |
| Method | camelCase verb+noun | `calculateTrailingReturns()` |
| Boolean | `is`/`has`/`can`/`should` prefix | `hasSectorData` |
| Variable | camelCase, intent-revealing | `elapsedDays` (not `d`) |
| Constant | UPPER_SNAKE, no magic literals | `MAX_RETRY_COUNT` |
| Enum factory | `fromValue(...)` | see CLAUDE.md |

## Functions
- **Small & single-purpose.** Extract steps into named methods; keep one level of abstraction per method.
- **Few params** — aim for ≤3; treat ≥6 as a smell (see table) → extract a command/parameter object or `@Builder`.
- **No flag arguments** — split `send(msg, urgent)` into `sendUrgent`/`queue`.
- **Guard clauses over nesting** — early-return on invalid input instead of pyramids of `if`.

## Comments
Explain **why**, not **what**. Delete comments that restate the code; make the code self-documenting instead (extract a well-named method/boolean). Keep comments that carry non-obvious rationale, warnings ("order matters: discounts before tax"), or dated TODOs.

## Common Code Smells → Refactoring

| Smell                                    | Fix |
|------------------------------------------|-----|
| Long method (> ~50 lines)                | Extract Method |
| Long parameter list (>= 6)               | Parameter Object |
| Duplicate code                           | Extract Method/Class |
| Magic number/string                      | Named constant / enum |
| God class                                | Extract Class |
| Feature envy (uses another class's data) | Move Method |
| Primitive obsession                      | Value object / `record` |
| Nested conditionals                      | Guard clauses |
| Dead code                                | Delete it |

**Antipatterns to avoid** (design-level traps, often surfacing as the smells above):
- **God object** — one class owning too many responsibilities → split by responsibility (SRP).
- **Anemic domain model** — data-only classes with all logic in services → move behaviour onto the type.
- **Golden hammer** — forcing one familiar tool/pattern everywhere → fit the pattern to the smell (KISS/YAGNI).
- **Reinventing the wheel** — custom code where a library or existing abstract base already does it → reuse (extend the calculation/service base).
- **Premature optimization** — complexity for unmeasured gains → measure first (see `performance-smell-detection`).
- **Shotgun surgery** — one change forcing edits across many classes → improve cohesion / centralize the concept.
- **Lava flow** — dead/obsolete code kept "just in case" → delete it.
- **Hardcoding** — magic numbers/strings and environment values baked in → constants/enums/config.

## Related
- **`coder`** — hexagonal + repo conventions when writing the code
- **`code-reviewer`** / **`review-lessons`** — reviewing quality; concrete defects already caught here
