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
Fetches data from Security Master (SM) via REST, performs financial calculations.
**No database, no cache, no GraphQL.**

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

---

## Module Structure & Rules

The project implements the Hexagonal Architecture with Spring Boot with the following modules:

| Module               | Purpose                                                   | Spring? |
|----------------------|-----------------------------------------------------------|---------|
| `domain`             | Pure domain models, enums, DTOs, calculation types        | No      |
| `api`                | Port interfaces, shared utilities, service interfaces     | No      |
| `application`        | Calculation orchestration, service impls, response mappers| Minimal |
| `rest-adapter`       | REST controllers, request/response DTOs, validation chain | Yes     |
| `web-client-adapter` | SM REST fetchers, mappers, stubs                          | Yes     |
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
- **Optional**: Return `Optional<T>` for optional values when it makes sense
- **Null check**: Validate SM data at adapter boundary with `Objects.requireNonNull`
- **Not null collections**: Never return null collections — use `List.of()`
- **Collection null/empty checks:** use `org.springframework.util.CollectionUtils.isEmpty(col)` instead of `col == null || col.isEmpty()`. Never perform the same `null || isEmpty` check twice in a row — collapse to a single `CollectionUtils.isEmpty` call
- **Object construction:** prefer Lombok builders (`@Builder` / `@SuperBuilder`) over chained accessors (`new Foo().setX(..).setY(..)`). Accessors are fine for incremental mutation inside mappers with conditional branches, but tests and one-shot construction must use the builder
- **Ternary:** use for simple single-expression returns/assignments instead of if/else
- **No `final`** on method parameters/variables unless class fields or explicit constants
- **No fully qualified class names** — always use imports
- **No magic strings** — extract to constants or enums
- **Enum factory methods:** always name `fromValue(value)`
- **Extract strings into constants or enums** - no magic strings in code
- **Extract repeated code into utility methods** - if you write the same 3+ lines twice, create a util
- **DI:** `@RequiredArgsConstructor` with final fields, not `@AllArgsConstructor`
- **Max 5-7 deps** per class; max 3 nesting levels (early returns)
- **Tests:** JUnit 5 + Mockito, no Spring context for unit tests. Naming: `shouldDoSomething_whenCondition`

## 4) External Calls (SM)

**Never call external web services in loops (N+1)** — always batch.
**Always use Resilience4j for resilience patterns**

```java
@CircuitBreaker(name = "securityMaster", fallbackMethod = "fetchFallback")
@Retry(name = "securityMaster")
@Bulkhead(name = "securityMaster")
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
4. Batch SM calls? -> No loops
5. Resilience configured? -> CircuitBreaker, Retry, Bulkhead
6. Config in YAML? -> No hardcoded values