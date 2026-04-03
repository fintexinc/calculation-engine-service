## Calculation Engine Service

Portfolio analytics and risk measurement engine. Calculates 50+ metrics including returns, risk, risk-adjusted performance, portfolio composition, fees, and forecasts.

Built with Java 21, Spring Boot 3.4.6, Hexagonal Architecture.

### Core Features

- **Return Analysis** — trailing, leading, rolling total returns, annual returns, growth of 10K
- **Risk Metrics** — standard deviation, downside deviation, max drawdown, beta, tracking error
- **Risk-Adjusted Ratios** — Sharpe, Sortino, Treynor, Information, MAR ratios
- **Relative Performance** — alpha, R-squared, correlation, upside/downside capture, excess returns
- **Portfolio Composition** — asset allocation, equity/fixed-income sector, country/geographic exposure, stylebox, market cap, credit quality, maturity
- **Fees & Holdings** — MER, management fees, sales charges, top common holdings
- **Forecasts** — income forecast, yield, distribution of monthly returns, best/worst periods

### Exposed API

Single unified endpoint for all 48 calculation metrics:

```
POST /api/v1/portfolio/calculations/{metric-name}
```

The `{metric-name}` path parameter selects the calculation. The request body schema depends on the metric type. See Swagger UI for full details and all available metric values.

You can read the description for all metrics, requests and responses on Swagger UI:

| Resource | Local URL                                                                                 |
|----------|-------------------------------------------------------------------------------------------|
| Swagger UI | `http://localhost:8181/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/swagger-ui/index.html` |
| OpenAPI YAML | `http://localhost:8181/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/api-docs.yaml`         |

| Resource | Remote URL                                                                                |
|----------|-------------------------------------------------------------------------------------------|
| Swagger UI | `https://calculation-engine-service.ashybay-bfa8feae.canadacentral.azurecontainerapps.io/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/swagger-ui/index.html` |
| OpenAPI YAML | `https://calculation-engine-service.ashybay-bfa8feae.canadacentral.azurecontainerapps.io/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/api-docs.yaml`         |

### Dependencies

| Dependency | Purpose |
|------------|---------|
| Security Master Service | Provides security data: allocations, sectors, exposures, credit quality, maturity |

### Module Structure

Hexagonal Architecture is used on this project.

| Module | Purpose |
|--------|---------|
| `domain` | Pure domain models and calculations (no Spring) |
| `api` | Port interfaces and shared DTOs (no Spring) |
| `application` | Use cases, orchestration via ports |
| `rest-adapter` | REST controllers exposing the API |
| `web-client-adapter` | REST client for Security Master |
| `bootstrap` | Spring Boot entry point and configuration |

### Prerequisites

- JDK 21
- Maven 3.6.2+
- Security Master Service (running locally or via Docker)

### Running the Service

Build:

```bash
./mvnw clean install
```

Run:

```bash
./mvnw spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=dev
```

### Running Security Master Service

There are two ways to run the Security Master dependency locally.

#### Option 1: Docker Compose

The `ce-environment/` folder contains a Docker Compose setup with the Security Master service and its database configuration.

```bash
cd ce-environment
docker compose up
```

This reads variables from `ce-environment/.env`. The `DB_URL` in the `.env` file uses `localhost` which works for local Maven runs. Docker Compose overrides it with `host.docker.internal` so the container can reach the host database.

`ce-environment/.env` variables:

| Variable | Description                                           |
|----------|-------------------------------------------------------|
| `SM_REST_BASE_URL` | Security Master base URL used by calculation-engine   |
| `DB_URL` | JDBC connection string for Security Master's database |
| `DB_USERNAME` | Database username                                     |
| `DB_PASSWORD` | Database password                                     |
| `DB_DIALECT` | Hibernate database dialect                            |
| `MIGRATION_SCRIPT_LOCATION` | Flyway migration scripts location                     |
| `FMP_API_URL` | Financial Modeling Prep API URL                       |
| `FMP_API_KEY` | Financial Modeling Prep API key                       |
| `MORNINGSTAR_CSV_BASE_PATH` | Full Path to Morningstar CSV data files               |

#### Option 2: Automatic Runner (dev profile)

When running calculation-engine with the `dev` profile, `SecurityMasterServiceRunner` automatically starts Security Master as a subprocess using Maven.

It expects the `security-master-service-v2` repository to be cloned next to this project. 
On startup, it loads the env file, launches `mvn spring-boot:run` in that directory, and stops the process when calculation-engine shuts down.

Runner configuration properties:

| Property | Default | Description |
|----------|---------|-------------|
| `sms.runner.enabled` | `true` | Enable/disable the automatic runner |
| `sms.runner.path` | `../security-master-service-v2` | Path to Security Master project |
| `sms.runner.env-file` | `environment-v2/.env` | Env file relative to Security Master project root |

**IMPORTANT**: `environment-ce/.env` overrides `environment-v2/.env` properties.

To disable the runner (e.g., when running Security Master via Docker instead):

```bash
./mvnw spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=dev -Dsms.runner.enabled=false
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `SM_REST_BASE_URL` | Security Master REST API base URL (e.g., `http://localhost:8080`) |
