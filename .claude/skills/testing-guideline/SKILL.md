---
name: testing-guideline
description: >
  Defines how to write tests (unit, integration, e2e) in this hexagonal-architecture Spring Boot repository.
  Use this skill whenever the user asks to write, add, create, fix, or update any test — unit tests, integration tests,
  or end-to-end tests. Also trigger when the user mentions test patterns, test placement, mocking strategy,
  test tagging, or MsSqlServerWithFtsContainer. Even if the user just says "add tests" or "cover this with tests",
  this skill applies.
---

# Spring Testing (Hexagonal Architecture)

This skill defines how to write tests in this repository: unit, integration, and end-to-end (e2e).
Follow these rules strictly.

---

## 1) Global rules (apply to all tests)

### 1.1 Naming convention (mandatory)

Don't write stupid useless comments like "when" "then" "assert" "helper method" etc. They do not bring any value.
You can write only meaningful comments that explain some complex code/tests.

All test methods MUST follow this pattern:

`shouldDoSmth_whenCondition...`

Examples:
- `shouldReturnUser_whenUserExists()`
- `shouldThrowException_whenInputIsInvalid()`
- `shouldPersistOrder_whenPaymentIsSuccessful()`

If multiple conditions are relevant, chain them:
- `shouldRejectTransfer_whenBalanceIsInsufficient_andLimitsExceeded()`

### 1.2 Arrange–Act–Assert (mandatory)
Use clear separation:
- Arrange: build inputs + stubs/mocks (unit only)
- Act: call one method / one action under test
- Assert: validate output and side-effects

### 1.3 Assertions quality
- **Unit tests:** MUST have **many assertions** validating the resulting data thoroughly (fields, invariants, boundary values).
- **Integration/e2e tests:** focus on key outcomes; many assertions are **nice-to-have**, not required.

### 1.4 Avoid duplicate near-identical tests and boilerplate tests

Always try to cover most lines with a minimum number of tests

How to minimize the number of duplicate tests:
- Use the Template Method pattern for hierarchical inheritance. Namely, AbstractCommonTest must contain all tests and final classes must only configure state, input and output expectations. 
E.g. AbstractSecurityEntityMapper, EtfEntityMapper etc.
- Can use `@ParameterizedTest` if only input and output differ (JUnit 5)
- Use `@MethodSource` for complex inputs
- Use `@CsvSource` / `@ValueSource` for simple scalar inputs

Where it's possible you can combine hierarchical test classes with implementation of the Template Method pattern and @ParameterizedTest annotation.

**Do NOT duplicate near-identical tests or tests with the same pattern (boilerplate).** it is a key rule.

### 1.5 Libraries and style
- JUnit 5 only.
- Prefer AssertJ assertions (`assertThat(...)`) for readability.
- Avoid `Thread.sleep`; use Awaitility or polling with timeouts for async behavior.
- Prefer explicit test data builders / fixtures over large inline object graphs.

---

## 2) Architecture mapping: where tests must live (Hexagonal)

The codebase follows Hexagonal Architecture. Place tests in the module that owns the behavior.

### 2.1 Unit tests
Unit tests can be defined in any module that owns the behavior.

Unit tests MUST NOT depend on:
- Spring context
- network
- database
- filesystem

### 2.2 Integration tests must be in the corresponding adapter modules (e.g. repository integration tests in jpa-adapter, cache tests in cache-adapter). Cross-module integration tests that require full Spring context live in the bootstrap module.
Integration tests should cover:
- adapters wiring (persistence, messaging, HTTP clients)
- Spring configuration
- repository implementations
- database integrations
- transactional boundaries

If integration tests need external services e.g. database or SFTP, they must create test containers for them.

### 2.3 E2E tests: bootstrap module only (mandatory)
E2E tests MUST live in the `bootstrap` module and cover:
- application running with HTTP endpoints (or messaging entrypoints)
- real database
- real external dependency containers when applicable
- test flows through the system boundary (API in → DB/out)

---

## 3) Mocking rules (strict)

### 3.1 Unit tests
- Unit tests MUST use mocks for external collaborators:
  - ports
  - repositories
  - gateways/clients
  - clocks/UUID providers, etc.
- Use Mockito (or the project's chosen mocking library).
- Verify interactions only when it represents a meaningful behavioral contract; otherwise prefer state assertions.

### 3.2 Integration tests
- Integration tests MUST NOT use mocks.
- No `@MockBean`, no Mockito stubs, no fake adapters.
- Use real beans + real persistence + real containers.

### 3.3 E2E tests
- E2E tests MUST NOT use mocks.
- Same rule: no `@MockBean`, no fake adapters.

If you need a controlled external dependency, use a containerized dependency or a deterministic test double service running as a container (not an in-process mock).

---

## 4) Tagging rules (mandatory)

### 4.1 Integration tests
All integration test classes MUST be annotated with:
- `@Tag("integration")`

### 4.2 E2E tests
All e2e test classes MUST be annotated with:
- `@Tag("e2e")`

Unit tests should typically have no tag (default).

---

## 5) Spring Boot test patterns

### 5.1 Unit tests (no Spring)
- Prefer plain JUnit tests without Spring.
- Construct the class under test manually.
- Inject mocks explicitly via constructor.

### 5.2 Integration tests (Spring context)
- Use `@SpringBootTest` (bootstrap module) when verifying real wiring.
- Use `@Transactional` where appropriate for cleanup; otherwise ensure cleanup per test.

### 5.3 E2E tests (HTTP boundary)
- Use `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` or the project's standard.
- Use a real HTTP client (e.g., `TestRestTemplate` or `WebTestClient` depending on stack).
- Validate request/response contracts and persistence side effects.

---

## 6) Database integration requirement (mandatory)

### 6.1 MSSQL with Full-Text Search for integration tests
All integration tests in the `bootstrap` module that require MSSQL MUST use:

`MsSqlServerWithFtsContainer`

Rules:
- Do not use a generic MSSQL container for these tests.
- Ensure FTS is available and initialized according to project conventions.
- Prefer schema migrations (Flyway/Liquibase) over ad-hoc SQL, unless explicitly needed for test setup.

---

## 7) What "good tests" look like (expectations)

### 7.1 Unit test expectations
Unit tests should cover:
- happy path
- validation failures
- boundary values
- domain invariants
- exception mapping (when applicable)
- branching logic

Unit tests should assert:
- multiple fields in returned objects, especially for extractors and mappers, they must assert as many fields as possible
- collection sizes and contents
- key invariants (e.g., totals, statuses, timestamps)
- side effects expressed via collaborator interactions (when meaningful)

### 7.2 Integration test expectations
Integration tests should validate:
- correct wiring of adapters
- persistence correctness (schema + queries + mappings)
- transactional behavior
- integration with MSSQL FTS (search queries return expected records)

Assertions should focus on:
- critical outputs and side effects
- a few representative data checks (not exhaustive)

### 7.3 E2E test expectations
E2E tests should validate:
- external API contract (status codes, payload shape)
- the main user/system flows
- basic resilience (idempotency where relevant)
- real persistence effects

Keep e2e suite smaller than integration suite; keep integration suite smaller than unit suite.

---

## 8) Anti-patterns (do not do these)

- Using Spring context in unit tests.
- Using mocks in integration/e2e tests (including `@MockBean`).
- Copy-pasting similar tests instead of `@ParameterizedTest`.
- Weak unit tests with only 1 assertion when multiple invariants exist.
- Flaky tests relying on timing/sleeps.
- Putting integration tests outside their corresponding module (adapter-specific tests in adapter modules, cross-module tests in bootstrap).

---

## 9) Default templates to follow

### Unit test skeleton
- `shouldDoSmth_whenCondition...`
- many assertions
- mocks for dependencies
- no Spring

### Integration test skeleton
- annotated with `@Tag("integration")`
- placed in the corresponding adapter module (or bootstrap for cross-module tests)
- uses `MsSqlServerWithFtsContainer` when DB is required
- no mocks

### E2E test skeleton
- annotated with `@Tag("e2e")`
- placed in bootstrap module
- real HTTP boundary
- no mocks

---

## 10) Execution notes
- Unit tests must be fast and run on every local iteration.
- Integration tests may be slower (containers). Still aim to keep them deterministic.
- E2E tests are the slowest; keep them focused on critical flows.

Always run the appropriate subset locally before finishing a change:
- unit tests for touched logic
- integration tests for adapter/db changes
- e2e tests for boundary changes

---

## Related
- **`coder`** — the code under test and repo implementation conventions
- **`code-reviewer`** — PR-readiness test/coverage checks
- **`review-lessons`** — concrete test defects previously caught in review (`[test-quality]` items)
- **`clean-code`** — general principles the tests exercise
- [AssertJ docs](https://assertj.github.io/doc/) · [JUnit 5 guide](https://junit.org/junit5/docs/current/user-guide/)
