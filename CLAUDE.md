# Calculation Engine Service

## Project Overview

Portfolio calculation microservice for financial analysis — computes returns, risk metrics, allocations, and exposures.
Java 21, Spring Boot 3.4.6, multi-module Maven project using Hexagonal Architecture (Ports & Adapters).

## Build & Test

```bash
mvn clean install                          # Full build with tests
mvn clean install -DskipTests=true         # Build without tests
mvn test                                   # Run all tests
mvn test -pl application                   # Run tests in one module
mvn test -pl application -Dtest=SharpeRatioCalculationServiceImplTest  # Run single test class
mvn compile -pl <module> -am              # Compile single module with dependencies
mvn spotless:apply                         # Format code
mvn spotless:check                         # Check formatting
mvn spring-boot:run -D"spring-boot.run.profiles"=localdev  # Run locally
mvn jib:build                              # Build Docker image
```

## Module Structure

| Module               | Purpose                                                                       | Spring allowed? |
|----------------------|-------------------------------------------------------------------------------|-----------------|
| `domain`             | Pure domain models, enums, DTOs, calculation types                            | No              |
| `api`                | Port interfaces (input/output), shared utilities, service interfaces          | No              |
| `application`        | Use cases: calculation orchestration, service impls, response mappers         | Minimal         |
| `rest-adapter`       | REST controllers, request/response DTOs, validation chain                     | Yes             |
| `web-client-adapter` | Security Master REST fetchers, mappers, stubs for unimplemented data sources  | Yes             |
| `bootstrap`          | Spring Boot entry point, bean wiring, property configuration                  | Yes             |

## Architecture Rules

- **domain** and **api** must NOT depend on Spring Framework (no spring-context, no @Component, no @Scheduled)
- Adapters must not call each other directly — communicate through ports
- Application layer uses port interfaces, never concrete adapters
- Use `@Service`/`@Component` in the application module and adapters for bean registration
- Use `@Bean` in `@Configuration` classes only for beans created via loops or factory logic
- External service calls must use Resilience4j annotations (@CircuitBreaker, @Retry, @Bulkhead)
- Never call external services in loops (N+1 problem) — use batch operations

## Calculation Architecture (Two-Layer Pattern)

Calculations use a **Service → Calculation** two-layer design with Template Method pattern:

### Service Layer (`application/.../service/calculation/`)
Orchestrates data fetching and delegates to calculation objects.

- **`PeriodAbstractService<E, R>`** — base for period-based calculations (returns, ratios). Method: `perform(command)` → `defineCalculationMethod()` (abstract) → `calculate(periods)`
- **`PeriodBenchmarkAbstractService<E, R>`** — extends above, adds benchmark data fetching with `Notification` error handling
- **`BreakdownAbstractService<T, E>`** — base for allocation/exposure calculations. Flow: `fetchExposures()` → `calculate()` with `calculateNetProducts()`

### Calculation Layer (`application/.../calculation/core/`)
Pure calculation logic, no data fetching.

- **`PeriodCalculationAbstract<T, V>`** — handles period resolution (numeric months, YTD, SINCE_INCEPTION), date filtering. Abstract: `calculatePeriodForNumberOfMonths()`
- **`RollingAbstractCalculation<T>`** — extends above for rolling window calculations (rolling returns, rolling Sharpe, etc.)
- **`AlphaBetaCalculationAbstract<T>`**, **`RSquaredCalculationAbstract<T>`** — portfolio-vs-benchmark regression calculations
- **`UpDownSideCalculationAbstract`** — upside/downside capture ratios

### Adding a New Calculation
1. Create domain result type in `domain/.../result/`
2. Create calculation class extending appropriate abstract base in `application/.../calculation/`
3. Create service impl extending appropriate abstract service in `application/.../service/calculation/`
4. Add response mapper in `application/.../mapper/response/`
5. Add endpoint in `PortfolioController`, request/response DTOs in `rest-adapter/.../dto/`
6. Wire in `PortfolioController` constructor via `@Qualifier`

## REST Layer Flow

```
PortfolioController → RequestValidationFacade (Chain of Responsibility)
    → RestCommandMapper (DTO → domain command)
    → CalculationService.perform(command)
    → RestResponseMapper (domain result → response DTO)
```

All portfolio endpoints are POST on `/portfolio/*`. Validation uses a chain: `NotNullReqValidation` → `HoldingsCouldNotBeEmptyReqValidation` → `DateGreaterThanDateAbstractReqValidation` → `LastDayOfMonthAbstractReqValidator`.

## Data Fetching Pattern (Web Client Adapter)

**`AbstractSecurityMasterFetcher<DomainModel, SmsResponse>`** — template for all Security Master REST fetchers:
- `endpointPath()` — REST endpoint path (from properties)
- `responseType()` — `ParameterizedTypeReference` for deserialization
- `responseMapper()` — maps SM response to domain model
- Groups holdings by `FinancialInstrumentType`, builds `IdsAndDataProvidersRequest`, calls SM API

Concrete fetchers activated via `@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest")`.

**Stubs** (`web-client-adapter/.../stub/`): `FxRatesFetcherStub`, `TBillsFetcherStub` — return empty data, to be replaced with real REST implementations.

## Key Packages

- Port interfaces (SM): `api/.../port/sm/` — `SecurityDataFetcher<T>`
- Port interfaces (other): `api/.../port/` — `FxRatesFetcher`, `TBillsFetcher`
- Service interfaces: `api/.../service/calculation/` — `CalculationService`, `PeriodCalculationService`, `BreakdownCalculationService`
- Utilities: `api/.../util/` — `AllocationMappingUtils`, `CalculationUtils`, `DecimalUtils`, `DateTimeUtils`, `PortfolioUtils`, `FilterUtils`
- Domain commands: `domain/.../dto/command/` — `PeriodCommand`, `PortfolioHoldingsCommand`, `RollingCalculationCommand`, etc.
- Domain results: `domain/.../model/result/` — `PeriodResult` (base), 40+ concrete result types
- Allocation type enums: `domain/.../model/calculation/` — `MaturityAllocationType`, `EquityMarketCapType`, `EquityStyleboxType`, etc.
- REST DTOs: `rest-adapter/.../adapter/rest/dto/request/` and `response/`
- Validators: `rest-adapter/.../adapter/rest/validation/chainofresponsibility/`
- SM fetchers: `web-client-adapter/.../sm/fetcher/` — extend `AbstractSecurityMasterFetcher`
- SM client: `web-client-adapter/.../sm/client/SecurityMasterWebClient`
- Bootstrap configs: `bootstrap/.../config/`

## Conventions

- Tests: JUnit 5 + Mockito, no Spring context for unit tests
- Test naming: `shouldDoSomething_whenCondition`
- Commits: Conventional Commits format:
  ```
  <type>(<scope>): <subject>

  <body — separate paragraph>

  refs: CE-123
  ```
- PRs: 1-2 squashed commits, rebase onto main, fast-forward merge
- Code formatting: Spotless with Eclipse formatter (`eclipse-java-formatter.xml` in root), 2-space indent, 120 char lines
- BigDecimal: use `BigDecimal.valueOf()` for numeric literals, never `new BigDecimal(double)`. `new BigDecimal(String)` is fine.
- Collections: use Stream API with `Collectors` — never use for-loops or `forEach` with manual `add()`/`put()` into a new collection

## Dependencies (sm-domain)

- `com.fintex.wm:domain:2.0.0-SNAPSHOT` provides: StyleBoxValue, StyleBoxes, PaymentFrequencyType, SalesChargeType, FxRate, SecurityIdentifier
- Version must be explicit in `domain/pom.xml` for transitive resolution
- Maven repository: Azure DevOps (`pkgs.dev.azure.com/fintexincorporated`)

## Known Gotchas

- Lombok `@Accessors(chain=true)` on parent class: setter returns parent type, breaks fluent chains in subclasses
- Holding class hierarchy cannot be mocked with Mockito (ByteBuddy can't instrument sm-domain SecurityIdentifier)
- When removing constructor params from services, update ALL test files using `useConstructor()`
- Removing transitive dependencies may require adding explicit deps (slf4j-api, spring-web)
