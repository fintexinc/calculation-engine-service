---
name: api-contract-review
description: Review REST API contracts for HTTP semantics, versioning, backward compatibility, and response consistency. Use when user asks "review API", "check endpoints", "REST review", or before releasing API changes.
---

# API Contract Review

Audit the REST contract for correctness, consistency, and backward compatibility.

## This repo's shape (read before reviewing)
There is **one controller** — `PortfolioCalculationController` — serving `POST /api/v1/portfolio/calculations/{metric-name}` (already versioned). Metrics are dispatched by the **Strategy pattern**: `CalculationMetric` enum (kebab-case) → command DTO → a `CalculationService<Command, Result>` bean. **Adding/changing a metric never touches the controller or adds an endpoint.** So "API contract" here almost always means the **command (request) and result (response) DTOs, validation, status codes, and JSON backward compatibility** — not URL/verb design. Keep generic REST advice below in reserve for when genuinely new HTTP surface is added.

## When to Use
- Reviewing a PR that adds/changes a metric's command or result DTO
- Reviewing validation or error-response behavior
- Checking backward compatibility before release

## Highest-value checks here

**Request (command DTO)**
- [ ] New command fields are **optional with a default** — a required field breaks existing callers.
- [ ] Validation is a `RequestValidator` registered for the metric via `supportedMetrics()`, collecting errors through `PceExceptionCollector` and throwing them all at once (not fail-fast, not in the controller).
- [ ] Security identifiers validated (id + idType present/non-blank) at the boundary for holdings that need an MIC lookup — follow `HoldingsValidationHelper` (rest-adapter).
- [ ] Command is an immutable value carrier (`record` / `@Builder`, no setters).

**Response (result DTO)**
- [ ] Absent/unknown upstream data returns a documented shape (null buckets + a warning) rather than silently-zeroed values — matches recent `geographic-exposure`/`equity-sector`/`max-drawdown` fixes.
- [ ] Consistent shape across metrics (same envelope, same null/warning conventions).

## Status codes

| Code | Use | Common mistake |
|------|-----|----------------|
| 200 | Successful calculation | — |
| 400 | Invalid/malformed input, validation failure | Using for "upstream data missing" |
| 404 | Unknown `metricName` | Returning 400 |
| 422 | Syntactically valid but semantically impossible request | Overloading 400 |
| 500 | Unexpected server error | Leaking stack traces to the client except dev/test profiles |

## Error response format
Single consistent structure via the global handler: machine-readable `code`, human `message`, `timestamp`, `path`, and field-level `errors` for validation. Log full details server-side; **never** put stack traces or internal identifiers in the response body — except under dev/test profiles.

## Generic REST reserve (only if new HTTP surface is added)
- Conform to the HTTP specification — [RFC 9110](https://datatracker.ietf.org/doc/html/rfc9110): method semantics (safe/idempotent), status codes, and headers as defined there.
- URLs: versioned, plural nouns not verbs (`/users` not `/getUsers`), hierarchical relationships, one casing convention.
- Collections: paginate; don't return unbounded `findAll()`.

## Related
- **`coder`** — command/result DTO + validator wiring conventions
- **`code-reviewer`** — full review pass
- `security-audit` — no internal leakage in error responses
