# Calculation Engine Service

## Project Overview
Java 21, Spring Boot 3.4.6, multi-module Maven project using Hexagonal Architecture (Ports & Adapters).

## Build & Test
```bash
./mvnw clean install          # Full build (use ./mvnw, not mvn)
./mvnw test                   # Run all tests
./mvnw compile -pl <module>   # Compile single module
./mvnw spotless:apply         # Format code
./mvnw spotless:check         # Check formatting
```

## Module Structure

| Module               | Purpose                                                                       | Spring allowed? |
|----------------------|-------------------------------------------------------------------------------|-----------------|
| `domain`             | Pure domain models & calculations                                             | No              |
| `api`                | Port interfaces (input/output) + shared DTOs                                  | No              |
| `application`        | Use cases, orchestration (uses ports only)                                    | Minimal         |
| `rest-adapter`       | Exposes REST API to consumers (driving adapter)                               | Yes             |
| `web-client-adapter` | Retrieves data from Security Master via REST (partly implemented, many stubs) | Yes             |
| `bootstrap`          | Spring Boot entry point, wiring, bean configs                                 | Yes             |

## Architecture Rules
- **domain** and **api** must NOT depend on Spring Framework (no spring-context, no @Component, no @Scheduled)
- Adapters must not call each other directly — communicate through ports
- Application layer uses port interfaces, never concrete adapters
- Use `@Service`/`@Component` in the application module and adapters for bean registration
- Use `@Bean` in `@Configuration` classes only for beans created via loops or factory logic
- External service calls must use Resilience4j annotations (@CircuitBreaker, @Retry, @Bulkhead)
- Never call external services in loops (N+1 problem) — use batch operations

## Key Packages
- Port commands/results: `api/.../port/input/command/` and `result/`
- Output ports (SM): `api/.../port/output/sm/` — `SecurityDataFetcher<T>`
- Service interfaces: `api/.../service/`
- REST DTOs: `rest-adapter/.../adapter/rest/dto/`
- Validators: `rest-adapter/.../adapter/rest/validation/`
- SM fetchers: `web-client-adapter/.../sm/fetcher/` — extend `AbstractSecurityMasterFetcher`
- SM stubs: `web-client-adapter/.../stub/` — temporary stubs returning empty data (to be replaced)
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
- Code formatting: Spotless with Eclipse formatter (2-space indent, 120 char lines)
- BigDecimal: use `BigDecimal.valueOf()` for numeric literals, never `new BigDecimal(double)` (avoids floating-point representation issues). `new BigDecimal(String)` is fine.
- Collections: use Stream API with `Collectors` to build/transform collections — never use for-loops or `forEach` with manual `add()`/`put()` into a new collection

## Dependencies (sm-domain)
- `com.fintex.wm:domain:2.0.0-SNAPSHOT` provides: StyleBoxValue, StyleBoxes, PaymentFrequencyType, SalesChargeType, FxRate, SecurityIdentifier
- Version must be explicit in `domain/pom.xml` for transitive resolution

## Known Gotchas
- Lombok `@Accessors(chain=true)` on parent class: setter returns parent type, breaks fluent chains in subclasses
- Holding class hierarchy cannot be mocked with Mockito (ByteBuddy can't instrument sm-domain SecurityIdentifier)
- When removing constructor params from services, update ALL test files using `useConstructor()`
- Removing transitive dependencies may require adding explicit deps (slf4j-api, spring-web)