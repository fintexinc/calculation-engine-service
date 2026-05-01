# Contributing to Fintex Portfolio Calculation Engine

Thank you for contributing to the Fintex Portfolio Calculation Engine! This document provides guidelines and best
practices for contributing to this project.

## Table of Contents

- [Code Style Guidelines](#code-style-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Development Workflow](#development-workflow)

## Code Style Guidelines

### Always try to use GoF patterns adapted to the Spring ecosystem

E.g. Strategy (inject a list of beans of an interface and associate them to map by common method returning an enum),
Chain of Responsibility, Template Method, Observer, Factory Method, Adapter, Bridge, etc.

### Calculation Architecture (Two-Layer Pattern)

Service -> Calculation two-layer design with Template Method:

### Service Layer (`application/.../service/calculation/`)
- **`PeriodAbstractService<E, R>`** — period-based calculations (returns, ratios). `perform(command)` -> `defineCalculationMethod()` -> `calculate(periods)`
- **`PeriodBenchmarkAbstractService<E, R>`** — adds benchmark data with `Notification` error handling
- **`BreakdownAbstractService<T, E>`** — allocation/exposure. `fetchExposures()` -> `calculate()` -> `calculateNetProducts()`

### Calculation Layer (`application/.../calculation/metric`)
Pure logic, no data fetching.
- **`PeriodCalculationAbstract<T, V>`** — period resolution (months, YTD, SINCE_INCEPTION), date filtering
- **`RollingAbstractCalculation<T>`** — rolling window calculations
- **`AlphaBetaCalculationAbstract<T>`**, **`RSquaredCalculationAbstract<T>`** — regression
- **`UpDownSideCalculationAbstract`** — capture ratios

### Code convention rules
- **Formatting:** Spotless with Eclipse formatter (`eclipse-java-formatter.xml`), 2-space indent, 120 char lines. Run `mvn spotless:apply`
- **BigDecimal:** `BigDecimal.valueOf()` for literals, never `new BigDecimal(double)`. `new BigDecimal(String)` is fine
- **Collections:** Stream API with `Collectors` — never for-loops/forEach with manual add/put
- **Stream to list:** prefer `.toList()` (returns an unmodifiable list) over `.collect(Collectors.toList())`. Only use `Collectors.toList()` when the result must be mutable
- **Optional**: Return `Optional<T>` for optional values when it makes sense
- **Null check**: Validate SM data at adapter boundary with `Objects.requireNonNull`
- **Not null collections**: Never return null collections — use `List.of()`
- **Collection null/empty checks:** use `org.springframework.util.CollectionUtils.isEmpty(col)` instead of `col == null || col.isEmpty()`. Never perform the same `null || isEmpty` check twice in a row — collapse to a single `CollectionUtils.isEmpty` call
- **Object construction:** prefer immutable data classes. Construct via the canonical/all-args constructor, a single-field constructor (or named static factory `ofX(...)` when types would collide) for the dominant case, or a Lombok `@Builder` / `@SuperBuilder` for multi-field cases; avoid setter-based construction and never mix builder calls with post-build setters. For pure value carriers, use `record`s.
- **Ternary:** use for simple single-expression returns/assignments instead of if/else
- **No `final`** on method parameters/variables unless class fields or explicit constants
- **No fully qualified class names** — always use imports
- **No magic strings** — extract to constants or enums
- **Enum factory methods:** always name `fromValue(value)`
- **DI:** `@RequiredArgsConstructor` with final fields, not `@AllArgsConstructor`
- **Extract strings into constants or enums** - no magic strings in code
- **Extract repeated code into utility methods** - if you write the same 3+ lines twice, create a util


### Code Formatting

All Java code must follow the project's formatting rules defined in `eclipse-java-formatter.xml`. We use Spotless with
the Eclipse formatter. Before creating a pull request:

```bash
# Format all code
mvn spotless:apply

# Check formatting without applying changes
mvn spotless:check
```

The CI pipeline includes a formatting check that will fail if code is not properly formatted.

**Formatting guidelines:**

- Use 2 spaces for indentation
- Maximum line length: 120 characters
- Braces on same line (end_of_line style)
- Each enum constant on a new line
- Simple if statements can be on one line: `if (this == o) return true;`
- Method chains preserve existing formatting
- Use Java 21 features where appropriate (records, pattern matching, etc.)
- Follow hexagonal architecture patterns established in the codebase

### Test Naming Convention

Test methods must follow the `shouldDoSomething_whenCondition` naming pattern. The name should clearly describe the
expected behavior and the condition that triggers it.

**Examples:**

```java
@Test
void shouldCalculateReturns_whenSecurityDataExists() { ... }

@Test
void shouldThrowException_whenSecurityNotFound() { ... }

@Test
void shouldFetchFromSM_whenSecurityIdIsValid() { ... }
```

### Avoid Duplicate Tests

- Use `@ParameterizedTest` when same logic with different inputs
- Use abstract test classes with Template Method pattern when testing similar implementations
- Before writing a new test, check if a similar test exists that can be extended

## Commit Message Guidelines

### Conventional Commits

We follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification for all commit
messages.

**Format:**

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code changes that neither fix a bug nor add a feature
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Changes to build process, dependencies, or tooling
- `ci`: Changes to CI/CD configuration

### Ticket References

All commits must include a ticket reference in the footer using one of these formats:

- `refs: <ticket-number>` - For general references
- `fixes: <ticket-number>` - For bug fixes
- `closes: <ticket-number>` - For feature completion

You can include several ticket numbers if a commit relates to them.

**Examples:**

```
feat(domain): add Sharpe ratio calculation

Implement Sharpe ratio calculation extending PeriodCalculationAbstract.
Uses existing template method pattern for consistency.

refs: CE-123
```

```
fix(web-client): add circuit breaker to SM endpoint

Added Resilience4j annotations to prevent cascading failures
when Security Master is unavailable.

fixes: CE-456, CE-457
```

```
chore(deps): update Spring Boot to 3.5.8

refs: CE-789
```

## Pull Request Process

### Branch Strategy

We use a rebase and fast-forward merge approach to maintain a linear git history.

1. **Create a feature branch from main:**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feat/your-feature-name
   ```

2. **Make your changes and commit following the guidelines above**

3. **Before creating a PR, rebase onto main:**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

4. **If conflicts occur, resolve them and continue:**
   ```bash
   # Resolve conflicts in your editor
   git add .
   git rebase --continue
   ```

5. **Force push your rebased branch (only for feature branches):**
   ```bash
   git push origin feat/your-feature-name --force-with-lease
   ```

### Commit Squashing

Each PR should contain **1-2 main commits** maximum. If you have multiple commits, squash them before creating the PR:

```bash
# Interactive rebase to squash last N commits
git rebase -i HEAD~N

# In the editor, change 'pick' to 'squash' (or 's') for commits you want to combine
# Save and close the editor
# Edit the combined commit message
```

**Example scenario:**

You have 5 commits:

```
* feat: add calculation endpoint (latest)
* fix: typo in formula
* refactor: extract method
* test: add unit tests
* feat: initial implementation
```

Squash them into 1-2 meaningful commits:

```
* feat(domain): add Sharpe ratio calculation with tests
```

### Handling Review Feedback

**IMPORTANT:** Maintain the 1-2 commit limit even after fixing review comments.

When fixing code review feedback:

1. **Make the requested changes**

2. **Amend the existing commit(s)** instead of creating new "fix review comments" commits:
   ```bash
   # Make your changes
   git add .
   git commit --amend --no-edit  # Keep the same commit message

   # Or edit the commit message if needed
   git commit --amend
   ```

3. **Force push the updated branch:**
   ```bash
   git push origin feat/your-feature-name --force-with-lease
   ```

**Why this matters:**

- Prevents accidentally polluting the main branch with "fix review comments" or "address feedback" commits
- Maintains a clean, linear git history
- Each commit in main represents a complete, meaningful change

Developers can forget to squash commits before merge.

### Pull Request Checklist

Before creating a pull request, ensure:

- [ ] Code is formatted: `mvn spotless:apply`
- [ ] All tests pass: `mvn test`
- [ ] Branch is rebased onto latest main
- [ ] Commits follow Conventional Commits format
- [ ] Commits include ticket references
- [ ] PR contains 1-2 main commits
- [ ] PR description explains what and why (not how)
- [ ] No N+1 external service calls
- [ ] Resilience4j annotations on SM calls
- [ ] No hardcoded configuration values

## Development Workflow

### Running the Application

```bash
# Build entire project
mvn clean install

# Run with specific profile
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=devlocal

# Run tests
mvn test
```

### Module Structure (Hexagonal Architecture)

| Module               | Purpose                                                                       | Spring allowed? |
|----------------------|-------------------------------------------------------------------------------|-----------------|
| `domain`             | Pure domain models & calculations                                             | No              |
| `api`                | Port interfaces (input/output) + shared DTOs                                  | No              |
| `application`        | Use cases, orchestration (uses ports only)                                    | Minimal         |
| `rest-adapter`       | Exposes REST API to consumers (driving adapter)                               | Yes             |
| `web-client-adapter` | Retrieves data from Security Master via REST (partly implemented, many stubs) | Yes             |
| `bootstrap`          | Spring Boot entry point, wiring, bean configs                                 | Yes             |

**Architecture rules:**

- Domain must not import Spring or adapters
- Adapters must not call each other directly
- Application layer uses ports, not concrete adapters

### Code Review

All pull requests require:

- Approval from designated code owners (see CODEOWNERS file)
- Passing CI checks (tests, formatting, build)
- No unresolved conversations

#### Conversation Resolution Guidelines

**IMPORTANT:** Do not resolve conversations in advance.

Follow these rules when handling PR conversations:

1. **Only resolve a conversation if:**
    - You have implemented the requested change exactly as specified in the comment
    - The reviewer has explicitly approved your alternative approach
    - The discussion has reached a mutual agreement

2. **Do not resolve a conversation if:**
    - You're still waiting for reviewer feedback
    - You've implemented a different solution than requested (discuss first)
    - The conversation contains a question that hasn't been answered
    - You're unsure about the requested change

3. **When in doubt:**
    - Reply to the conversation explaining your approach
    - Ask for clarification if the feedback is unclear
    - Let the reviewer resolve the conversation after verifying your changes

**Why this matters:**

- Ensures all feedback is properly addressed
- Prevents miscommunication between author and reviewer
- Makes it clear which items still need attention
- Maintains accountability in the review process

### Merge Process

Once approved:

1. Ensure all CI checks pass
2. Maintainer will perform a fast-forward merge to main
3. Feature branch **will be deleted** after merge

## Questions?

If you have questions about these guidelines, please reach out to the maintainers or open a discussion issue.
