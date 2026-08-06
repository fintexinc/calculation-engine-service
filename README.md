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

### Exposed REST API

Single unified endpoint for all 48 calculation metrics:

```
POST /api/v1/portfolio/calculations/{metric-name}
```

The `{metric-name}` path parameter selects the calculation. The request body schema depends on the metric type. See Swagger UI for full details and all available metric values.

You can read the description for all metrics, requests and responses on Swagger UI. The exposed actuator endpoints —
including the per-metric statistics described under [Metrics & Observability](#metrics--observability) — are documented
there too, under the **Actuator** tag:

| Resource | Local URL                                                                                 |
|----------|-------------------------------------------------------------------------------------------|
| Swagger UI | `http://localhost:8181/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/swagger-ui/index.html` |
| OpenAPI YAML | `http://localhost:8181/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/api-docs.yaml`         |

| Resource | Remote URL                                                                                |
|----------|-------------------------------------------------------------------------------------------|
| Swagger UI | `https://portfolio-calculation-engine.ashybay-bfa8feae.canadacentral.azurecontainerapps.io/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/swagger-ui/index.html` |
| OpenAPI YAML | `https://portfolio-calculation-engine.ashybay-bfa8feae.canadacentral.azurecontainerapps.io/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/api-docs.yaml`         |

### Health Checks

Spring Boot Actuator exposes liveness and readiness probes — no custom controller. Suitable for Kubernetes / load-balancer health checks.

| Endpoint | Returns | Meaning |
|----------|---------|---------|
| `GET /actuator/health` | `200 {"status":"UP"}` / `503 {"status":"DOWN"}` | Composite of all registered indicators (SMS + Bank of Canada + built-ins) |
| `GET /actuator/health/liveness` | `200` / `503` | Process is alive (JVM up). Independent of downstream availability — never fails because of SMS/BoC outages. |
| `GET /actuator/health/readiness` | `200` / `503` | Service is ready to serve calculations. Gated on **SMS reachability** — every metric depends on it. Bank of Canada is *not* gated (only affects FX-conversion paths). |

Probe mechanics:

- A short fail-fast 3-second connect/read timeout is applied to the indicator HTTP calls (see `HealthCheckRestClientFactory`), so a hung downstream fails the probe quickly rather than holding up the K8s probe window.
- Endpoint paths called by the indicators are configurable: `external-services.security-master.rest.health-check-path` (default `/actuator/health`) and `external-services.bank-of-canada.health-check-path` (default `/lists/series/json`).
- `management.endpoint.health.show-details: never` — bodies don't leak SMS/BoC URLs or exception stacks (the actuator endpoint isn't behind auth).

Sample Kubernetes probe block:

```yaml
livenessProbe:
  httpGet: { path: /actuator/health/liveness, port: 8181 }
readinessProbe:
  httpGet: { path: /actuator/health/readiness, port: 8181 }
```

### Metrics & Observability

One actuator endpoint exposes runtime statistics. It is listed in Swagger UI under the **Actuator** tag and carries its
full response schema there, so it can be called with *Try it out*.

| Endpoint | Purpose |
|----------|---------|
| `GET /actuator/calculationstats` | Ranked per-metric calculation statistics — start here |

Exposure is controlled by `management.endpoints.web.exposure.include` (`health, info, calculationstats`).
`/actuator/metrics` is deliberately **not** exposed — it enumerates every meter name and tag value in the process, and
`ActuatorExposureConfigurationTest` pins it shut alongside `httpexchanges`. Read raw meters through a metrics exporter,
or expose the endpoint temporarily on a locally-run instance.

#### `/actuator/calculationstats` — the one to look at

Everything is keyed by the **calculation metric that actually ran**. A composite request is decomposed into its member
commands, so each member contributes its own row and the endpoint a client happened to call leaves no trace in the
numbers. Rows in `metrics` are ordered most-problematic first — by absolute failure count, then by failure ratio — so
the head of the list is the answer to "which metrics need attention".

A request rejected before dispatch — unknown metric, metric mismatch, a failed validation rule — never reached a
calculator and is counted nowhere here. `failureRatePercent` therefore measures the service, not the callers: a client
sending malformed requests cannot move it. Rejected requests are still visible as `4xx` on `http.server.requests` and
in the traces.

What to read, in order of usefulness:

1. **`metrics[].failureRatePercent` together with `failures`** — the health of each metric. A high ratio on meaningful
   volume is a real problem; 100% on two executions usually is not, which is why the list is sorted by absolute
   failures first.
2. **`metrics[].topErrorCodes`** — *why* a metric fails, as `ErrorCode` values (see `docs/error-codes.md`). Failures
   that carry no domain code fall back to the exception's simple name.
3. **`metrics[].duration.p95Millis` / `p99Millis`** — latency of the calculation itself, excluding request validation
   and Security Master fetching. Failed runs are recorded separately and never skew these, so a metric that fails fast
   does not look fast.
4. **`metrics[].warnings.mean` and `.max`** — data-quality pressure. A rising mean means the provider data is
   degrading even though requests still return `200`.
5. **`metrics[].topWarningCodes`** — which attribute is missing most often upstream.
6. **`overall`** — the same view across all metrics, for a single service-health number.

#### Underlying meters

What `/actuator/calculationstats` is built from, and what a metrics exporter would ship.

| Meter | Type | Tags |
|-------|------|------|
| `portfolio.calculation.executions` | Counter | `calculation.metric`, `outcome` |
| `portfolio.calculation.duration` | Timer | `calculation.metric`, `outcome` |
| `portfolio.calculation.errors` | Counter | `calculation.metric`, `error.code` |
| `portfolio.calculation.warnings` | DistributionSummary | `calculation.metric` |
| `portfolio.calculation.warnings.min` | Gauge | `calculation.metric` |
| `portfolio.calculation.warning.codes` | Counter | `calculation.metric`, `warning.code` |
| `portfolio.calculation.holdings` / `.benchmark.holdings` | DistributionSummary | `calculation.metric` |
| `portfolio.calculation.request` | Timer | `command.type`, `outcome`, `exception`, `result.type` |
| `external.provider.request` | Timer | `external.service`, `http.method`, `endpoint`, `outcome`, `exception`, `upstream.status` |
| `external.provider.result.size` | DistributionSummary | `external.service`, `endpoint` |
| `external.provider.request.failure.ratio` / `.duration.mean` | Gauge | `external.service`, `window` |
| `http.server.requests` | Timer | Spring Boot defaults |

Every external provider shares one meter name and is told apart by the `external.service` tag, so both an aggregate and
a per-provider view stay expressible without a dashboard having to enumerate the providers.

`outcome` on `external.provider.request` is `success`, `http_error` when the provider returned a response the client
rejected, or `error` when nothing came back at all — a connection failure, a timeout, an unparseable body. `upstream.status`
carries the real status for `http_error` and `none` otherwise; it is intentionally not the OpenTelemetry
`http.response.status_code` key, because a call that never reached the provider has no status to put there.

`portfolio.calculation.request` is the request-level timer shared by both endpoints — it deliberately has no
`calculation.metric` tag, because a composite request is not a metric. Per-metric latency lives in
`portfolio.calculation.duration`, which the orchestrator records around each individual metric.

p50/p95/p99 are computed client-side, configured in exactly one place —
`management.metrics.distribution.percentiles` — so they are readable directly from `calculationstats`. Micrometer does
not track a minimum for timers or distributions — `p50` serves that role; the one exception is
`portfolio.calculation.warnings.min`, which is a purpose-built gauge.

Counts, totals and means are cumulative; `max` and the percentiles come from the registry's rolling distribution window
and so describe recent traffic. No `management.metrics.distribution.expiry` override is set: widening that window would
only stretch how long a decaying `p95` disagrees with the cumulative `samples` count beside it. The rolling view of
external calls is served by its own purpose-built `failure.ratio` / `duration.mean` gauges, which state their window in
a `window` tag.

**Lifetime:** these counters live in the in-process meter registry. They reset on restart and are not currently shipped
to an external metrics backend — no exporting `MeterRegistry` is on the classpath. Distributed traces *are* exported to
Azure Application Insights (see below), so cross-service latency and failures are available there; per-metric counters
are local to the instance.

#### Tracing

Spans are exported to Azure Application Insights when a connection string is present, wired by
`AzureMonitorOpenTelemetryConfiguration`.

| Property | Default | Purpose |
|----------|---------|---------|
| `observability.azure-monitor.enabled` | `true` | Master switch |
| `observability.azure-monitor.connection-string` | `${APPLICATIONINSIGHTS_CONNECTION_STRING:}` | Blank disables export; the SDK bean is then not created at all |
| `observability.azure-monitor.live-metrics.enabled` | `true` | Application Insights live metrics stream |
| `otel.propagators` | `tracecontext,baggage,b3` | Accepted/emitted trace context formats |

Every log line carries `traceId`, `spanId` and `requestId`. `requestId` is taken from the inbound `X-Request-ID` header
(generated when absent), echoed back on the response, and forwarded to Security Master, so one identifier ties a client
report to both services' logs.

### Dependencies

| Dependency | Purpose |
|------------|---------|
| Security Master Service | Provides security data: allocations, sectors, exposures, credit quality, maturity |

### Module Structure

Hexagonal Architecture is used in this project.

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
