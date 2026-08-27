---
name: coder
description: >
  Coding skill for hexagonal architecture Spring Boot applications following clean code principles.
  Use this skill when writing new code, implementing features, fixing bugs, or adding functionality.
  Applies hexagonal architecture patterns, design patterns (Strategy, Template Method, Observer),
  clean code principles, N+1 prevention, null safety, and SonarQube compliance.
  Trigger when: "implement", "write", "add feature", "create", "fix", "build", "do", "refactor", "rework".
---

# Coding Guidelines

Portfolio calculation microservice: Java 21, Spring Boot 3.4.6, multi-module Maven, Hexagonal Architecture.
Fetches data from Market Investment Catalogue (MIC) via REST, performs financial calculations.
**No database, no GraphQL.** (Caching goes through the `cache-adapter` module.)

**Before finishing a change, scan the `review-lessons` skill** — a checklist of concrete defects
previously caught in review (e.g. edge-case regressions from moving logic, empty-collection
foot-guns). Add to it whenever a reviewer finds a new problem.

---

## Build & Test

```bash
mvn clean install                          # Full build with tests
mvn clean install -DskipTests=true         # Build without tests
mvn test -pl application                   # Tests in one module
mvn test -pl application -Dtest=SharpeRatioCalculationServiceImplTest  # Single test
mvn compile -pl <module> -am              # Compile module with deps
mvn spotless:apply                         # Format code
mvn spotless:check                         # Check formatting
mvn spring-boot:run -D"spring-boot.run.profiles"=dev  # Run locally
mvn jib:build                              # Build Docker image
```

### Troubleshooting: broken classes / ApplicationContext fails to load

If the build reports unresolved/broken classes from `ca.tangerine.wm` or the Spring
`ApplicationContext` fails to load (e.g. bean-wiring errors, `401 Unauthorized` fetching
`catalogue-investment-commons` metadata from Azure Artifacts), the local `catalogue-investment-commons`
is stale or missing. Update the `catalogue-investment-commons` project and install it into the local
Maven repo (`mvn clean install` in that project) so `~/.m2` has the current SNAPSHOT, then rebuild.

---

## Module Structure & Rules

The project implements the Hexagonal Architecture with Spring Boot with the following modules:

| Module               | Purpose                                                   | Spring? |
|----------------------|-----------------------------------------------------------|---------|
| `domain`             | Pure domain models, enums, DTOs, calculation types        | No      |
| `api`                | Port interfaces, shared utilities, service interfaces     | No      |
| `application`        | Calculation orchestration, service impls, response mappers| Minimal |
| `rest-adapter`       | REST controllers, request/response DTOs, validation chain | Yes     |
| `web-client-adapter` | MIC REST fetchers, mappers, stubs                          | Yes     |
| `bootstrap`          | Spring Boot entry point, bean wiring, config              | Yes     |

**Rules:**
- `domain` and `api` must NOT depend on Spring Framework (no spring-context, no @Component)
- Adapters must not call each other — communicate through ports
- Application layer uses port interfaces, never concrete adapters
- `@Service`/`@Component` for bean registration; `@Bean` in `@Configuration` only for factory/loop-created beans

---

## 1) Always try to use GoF patterns adapted to the Spring ecosystem

E.g. Strategy (inject a list of beans of an interface and associate them to map by common method returning an enum), 
Chain of Responsibility, Template Method, Observer, Factory Method, Adapter, Bridge, etc.

## 2) Calculation Architecture (Two-Layer Pattern)

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

## 3) Code Style & Conventions

- **Formatting:** Spotless with Eclipse formatter (`eclipse-java-formatter.xml`), 2-space indent, 120 char lines. Run `mvn spotless:apply`
- **BigDecimal:** `BigDecimal.valueOf()` for literals, never `new BigDecimal(double)`. `new BigDecimal(String)` is fine
- **Collections:** Stream API with `Collectors` — never for-loops/forEach with manual add/put
- **Stream to list:** prefer `.toList()` (returns an unmodifiable list) over `.collect(Collectors.toList())`. Only use `Collectors.toList()` when the result must be mutable
- **Optional**: Return `Optional<T>` for optional values when it makes sense
- **Null check**: Validate MIC data at adapter boundary with `Objects.requireNonNull`
- **Not null collections**: Never return null collections — use `List.of()`
- **Collection null/empty checks:** use `org.springframework.util.CollectionUtils.isEmpty(col)` instead of `col == null || col.isEmpty()`. Never perform the same `null || isEmpty` check twice in a row — collapse to a single `CollectionUtils.isEmpty` call
- **Object construction:** prefer immutable data classes. Construct via the canonical/all-args constructor, a single-field constructor (or named static factory `ofX(...)` when types would collide) for the dominant case, or a Lombok `@Builder` / `@SuperBuilder` for multi-field cases; avoid setter-based construction and never mix builder calls with post-build setters. For pure value carriers, use `record`s.
- **Ternary:** use for simple single-expression returns/assignments instead of if/else
- **No `final`** on method parameters/variables
- **No fully qualified class names** — always use imports
- **Keep Javadoc and comments short — 2-3 sentences.** Explain briefly *why* the code is the way it is; do not write essays. Multi-paragraph Javadoc with measurements, history and caveats belongs in the commit message or PR description, not above a method. If the explanation genuinely needs more than a few sentences, that is usually a sign the code should be simpler.
- **No ticket references in code** — never put a ticket id (e.g. `TMI-536`, `JIRA-123`) in a comment or Javadoc. Comments must explain the behavior/rationale on their own terms; ticket traceability belongs in commit messages and PRs, not in the source. (Applies to production code and tests alike.)
- **No magic strings** — extract to constants or enums
- **Enum factory methods:** always name `fromValue(value)`
- **Extract strings into constants or enums** - no magic strings in code
- **Extract repeated code into utility methods** - if you write the same 3+ lines twice, create a util
- **@UtilityClass:** Always use `@UtilityClass` for utility classes with only static methods
- **DI:** `@RequiredArgsConstructor` with final fields, not `@AllArgsConstructor`
- **Max 5-7 deps** per class; max 3 nesting levels (early returns)
- **Tests:** JUnit 5 + Mockito, no Spring context for unit tests. Naming: `shouldDoSomething_whenCondition`. Don't use `sut` for the system-under-test variable — pick a descriptive name from the type under test (e.g. `calculation`, `service`, `fetcher`, `mapper`).

## 4) External Calls (MIC)

**Never call external web services in loops (N+1)** — always batch.
**Always use Resilience4j for resilience patterns**

```java
@CircuitBreaker(name = "marketInvestmentCatalogue", fallbackMethod = "fetchFallback")
@Retry(name = "marketInvestmentCatalogue")
@Bulkhead(name = "marketInvestmentCatalogue")
```

No hardcoded timeouts, URLs, retry counts — configure in `application.yaml`.

---

## 5) Error Handling

All exceptions/instances must be linked to ErrorCode and handled in GlobalExceptionHandler. 
They must be converted to a Notification list.

## 6) Test Naming Convention

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

## Before Writing Code Checklist

1. Does similar code exist? -> Extend/reuse
2. Can this be abstracted? -> Template Method hierarchy
3. Correct module? -> Domain has no Spring
4. Batch MIC calls? -> No loops
5. Resilience configured? -> CircuitBreaker, Retry, Bulkhead
6. Config in YAML? -> No hardcoded values

## Related
- **`clean-code`** — during implementation, check the `clean-code` skill and follow its rules (SOLID, GoF, DRY/KISS/YAGNI, code smells & antipatterns) that these implementation rules build on