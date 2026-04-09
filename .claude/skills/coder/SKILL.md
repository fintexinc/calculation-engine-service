---
name: coder
description: >
  Coding skill for hexagonal architecture Spring Boot applications following clean code principles.
  Use this skill when writing new code, implementing features, fixing bugs, or adding functionality.
  Applies hexagonal architecture patterns, design patterns (Strategy, Template Method, Observer),
  clean code principles, N+1 prevention, null safety, and SonarQube compliance.
  Trigger when: "implement", "write", "add feature", "create", "fix", "build".
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

## 1) Think Hierarchy First

Before writing ANY class: "Can this be abstracted?"

### Template Method Pattern

```java
public abstract class SecurityDataFetcher<T extends Security> {
    public final T fetch(String id) {
        var rawData = fetchFromSM(id);
        validate(rawData);
        return mapToSecurity(rawData);
    }
    protected abstract T mapToSecurity(SMResponse data);
}
// Subclasses ONLY implement what's different
```

### Strategy Pattern

```java
public interface CalculationStrategy {
  boolean supports(SecurityType type);
  BigDecimal calculate(SecurityData data);
}

@Service
@RequiredArgsConstructor
public class CalculationService {
  private final List<CalculationStrategy> strategies;
  public BigDecimal calculate(Security security) {
    return strategies.stream()
            .filter(s -> s.supports(security.getType()))
            .findFirst()
            .orElseThrow(() -> new UnsupportedSecurityTypeException(security.getType()))
            .calculate(security.getData());
  }
}
```

**Existing abstract classes to extend:**
- `*CalculationAbstract` — calculations
- `AbstractSecurityMasterFetcher<DomainModel, SmsResponse>` — SM REST fetchers

---

## 2) Calculation Architecture (Two-Layer Pattern)

Service -> Calculation two-layer design with Template Method:

### Service Layer (`application/.../service/calculation/`)
- **`PeriodAbstractService<E, R>`** — period-based calculations (returns, ratios). `perform(command)` -> `defineCalculationMethod()` -> `calculate(periods)`
- **`PeriodBenchmarkAbstractService<E, R>`** — adds benchmark data with `Notification` error handling
- **`BreakdownAbstractService<T, E>`** — allocation/exposure. `fetchExposures()` -> `calculate()` -> `calculateNetProducts()`

### Calculation Layer (`application/.../calculation/core/`)
Pure logic, no data fetching.
- **`PeriodCalculationAbstract<T, V>`** — period resolution (months, YTD, SINCE_INCEPTION), date filtering
- **`RollingAbstractCalculation<T>`** — rolling window calculations
- **`AlphaBetaCalculationAbstract<T>`**, **`RSquaredCalculationAbstract<T>`** — regression
- **`UpDownSideCalculationAbstract`** — capture ratios

### Adding a New Calculation
1. Domain result type in `domain/.../result/`
2. Calculation class extending abstract base in `application/.../calculation/`
3. Service impl extending abstract service in `application/.../service/calculation/`
4. Response mapper in `application/.../mapper/response/`
5. Endpoint + DTOs in `rest-adapter/.../dto/`
6. Wire via `@Qualifier` in `PortfolioController`

---

## 3) REST Layer Flow

```
PortfolioController -> RequestValidationFacade (Chain of Responsibility)
    -> RestCommandMapper (DTO -> command) -> CalculationService.perform(command)
    -> RestResponseMapper (result -> response DTO)
```

All endpoints: POST `/portfolio/*`. Validation chain: `NotNullReqValidation` -> `HoldingsCouldNotBeEmptyReqValidation` -> `DateGreaterThanDateAbstractReqValidation` -> `LastDayOfMonthAbstractReqValidator`.

---

## 4) Module Placement & Ports

```java
// Output port (api module)
@FunctionalInterface
public interface SecurityDataFetcher<T> {
  Map<Holding, T> fetch(List<? extends Holding> holdings, List<DataProvider> providers);
}

// Abstract fetcher (web-client-adapter) — Template Method for SM REST calls
public abstract class AbstractSecurityMasterFetcher<DomainModel, SmsResponse>
    implements SecurityDataFetcher<DomainModel> {
  protected abstract String endpointPath();
  protected abstract ParameterizedTypeReference<List<SecurityAttributeResult<SmsResponse>>> responseType();
  protected abstract SecurityMasterResponseMapper<DomainModel, SmsResponse> responseMapper();
}
```

**Stubs** in `web-client-adapter/.../stub/`: return empty data. When implementing a real fetcher, extend `AbstractSecurityMasterFetcher` and remove the stub.

---

## 5) External Calls (SM)

**Never call SM in loops (N+1)** — always batch.
**Always use Resilience4j:**

```java
@CircuitBreaker(name = "securityMaster", fallbackMethod = "fetchFallback")
@Retry(name = "securityMaster")
@Bulkhead(name = "securityMaster")
```

No hardcoded timeouts, URLs, retry counts — configure in `application.yaml`.

---

## 6) Null Safety & Exceptions

- Return `Optional<T>` for optional values
- Validate SM data at adapter boundary with `Objects.requireNonNull`
- Never return null collections — use `List.of()`
- Translate exceptions at adapter boundaries with meaningful context messages

---

## 7) Code Style & Conventions

- **Formatting:** Spotless with Eclipse formatter (`eclipse-java-formatter.xml`), 2-space indent, 120 char lines. Run `mvn spotless:apply`
- **BigDecimal:** `BigDecimal.valueOf()` for literals, never `new BigDecimal(double)`. `new BigDecimal(String)` is fine
- **Collections:** Stream API with `Collectors` — never for-loops/forEach with manual add/put
- **Collection null/empty checks:** use `org.springframework.util.CollectionUtils.isEmpty(col)` instead of `col == null || col.isEmpty()`. Never perform the same `null || isEmpty` check twice in a row — collapse to a single `CollectionUtils.isEmpty` call
- **Object construction:** prefer Lombok builders (`@Builder` / `@SuperBuilder`) over chained accessors (`new Foo().setX(..).setY(..)`). Accessors are fine for incremental mutation inside mappers with conditional branches, but tests and one-shot construction must use the builder
- **Ternary:** use for simple single-expression returns/assignments instead of if/else
- **No `final`** on method parameters/variables unless class fields or explicit constants
- **No fully qualified class names** — always use imports
- **No magic strings** — extract to constants or enums
- **Enum factory methods:** always name `fromValue(value)`
- **DI:** `@RequiredArgsConstructor` with final fields, not `@AllArgsConstructor`
- **Max 5-7 deps** per class; max 3 nesting levels (early returns)
- **Tests:** JUnit 5 + Mockito, no Spring context for unit tests. Naming: `shouldDoSomething_whenCondition`
- **Commits:** Conventional Commits: `<type>(<scope>): <subject>` with `refs: CE-123`
- **PRs:** 1-2 squashed commits, rebase onto main, fast-forward merge

---

## 8) Key Packages Reference

- Port interfaces (SM): `api/.../port/sm/`
- Port interfaces (other): `api/.../port/`
- Service interfaces: `api/.../service/calculation/`
- Utilities: `api/.../util/` — `AllocationMappingUtils`, `CalculationUtils`, `DecimalUtils`, `DateTimeUtils`, `PortfolioUtils`, `FilterUtils`
- Domain commands: `domain/.../dto/command/`
- Domain results: `domain/.../model/result/` — 40+ result types
- Allocation enums: `domain/.../model/calculation/`
- REST DTOs: `rest-adapter/.../adapter/rest/dto/`
- Validators: `rest-adapter/.../adapter/rest/validation/chainofresponsibility/`
- SM fetchers: `web-client-adapter/.../sm/fetcher/`
- SM client: `web-client-adapter/.../sm/client/SecurityMasterWebClient`
- Bootstrap configs: `bootstrap/.../config/`

---

## 9) Dependencies & Gotchas

**sm-domain:** `com.fintex.wm:domain:2.0.0-SNAPSHOT` provides: StyleBoxValue, StyleBoxes, PaymentFrequencyType, SalesChargeType, FxRate, SecurityIdentifier. Version must be explicit in `domain/pom.xml`.

**Gotchas:**
- Lombok `@Accessors(chain=true)` on parent: setter returns parent type, breaks fluent chains in subclasses
- Holding class hierarchy cannot be mocked (ByteBuddy can't instrument sm-domain SecurityIdentifier)
- When removing constructor params from services, update ALL test files using `useConstructor()`
- Removing transitive dependencies may require adding explicit deps (slf4j-api, spring-web)

---

## Before Writing Code Checklist

1. Does similar code exist? -> Extend/reuse
2. Can this be abstracted? -> Template Method hierarchy
3. Correct module? -> Domain has no Spring
4. Batch SM calls? -> No loops
5. Resilience configured? -> CircuitBreaker, Retry, Bulkhead
6. Config in YAML? -> No hardcoded values