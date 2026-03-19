---
name: code-reviewer
description: >
  Code review skill for hexagonal architecture Spring Boot applications.
  Use this skill when the user asks to review code, check code quality, find issues,
  identify problems, suggest improvements, or audit code. Also trigger when the user
  mentions code review, PR review, quality check, or asks "what's wrong with this code".
  Provides severity-based feedback from Critical to Low priority.
---

# Code Review Guidelines

Calculation engine service: fetches data from Security Master (SM) via REST, performs financial
calculations. **No database, no cache, no GraphQL.**

---

## Severity Levels

| Severity     | Must Fix?         | Examples                                                            |
|--------------|-------------------|---------------------------------------------------------------------|
| **Critical** | Yes, now          | Missing timeouts, N+1 external calls, null pointers, resource leaks |
| **High**     | Yes, before merge | Architecture violations, missing resilience, code duplication       |
| **Medium**   | Should fix        | Hardcoded config, excessive dependencies, poor error handling       |
| **Low**      | Optional          | Naming, documentation, minor style                                  |

---

## Critical Issues

### Missing Abstraction / Code Duplication

**This is the #1 issue to catch.** If you see 5+ classes with similar structure:

```java
// BAD: 20 endpoint classes with identical fetch() implementation
public class EtfEndpoint extends AbstractEndpoint<EtfResponse> {
    public EtfResponse fetch(String id) {
        return client.query(id, EtfResponse.class);  // Same in ALL classes!
    }
}
```

**Fix:** Move common logic to abstract class. Subclasses only provide type-specific details.

### N+1 External Calls

```java
// BAD
items.forEach(i -> smClient.fetch(i.getId()));
// GOOD
smClient.fetchBatch(ids);
```

### Missing Timeouts/Resilience

All SM calls need: `@CircuitBreaker`, `@Retry`, `@Bulkhead`

```java
// Required pattern
@CircuitBreaker(name = "securityMaster", fallbackMethod = "fallback")
@Retry(name = "securityMaster")
@Bulkhead(name = "securityMaster")
public Data fetch(String id) { }
```

### Null Pointer Risks

- Accessing fields from SM response without null checks
- `Optional.get()` without `isPresent()` or `orElse`
- Missing `Objects.requireNonNull` for required parameters

### Resource Leaks

- Unclosed HTTP connections or streams
- Missing try-with-resources for AutoCloseable

---

## High Issues

### Architecture Violations

- Domain importing Spring/adapters/SM DTOs
- Adapter calling another adapter directly
- Business logic in REST controller
- Application layer using adapters directly instead of ports

### Missing Hierarchy

When similar classes could share an abstract parent:

```
Security hierarchy should be:
Security (abstract)
├── TradableSecurity (abstract, simplified) → Etf, Fund, Stock, Index...
└── ...
```

### Hardcoded Configuration

Timeouts, URLs, retry counts must be in `application.yaml`, not code:

```java
// BAD
private static final int TIMEOUT_MS = 30000;
private static final String SM_URL = "http://sm-service/api";

// GOOD - use @ConfigurationProperties or @Value
```

### Missing Fallback

External service calls without fallback method for when SM is unavailable.

---

## Medium Issues

- Too many dependencies (>5-7) → split the class
- Generic exception catching → use specific types
- SM exceptions not translated to domain exceptions
- Magic strings instead of constants/enums
- Repeated code blocks that should be utility methods
- Missing input validation at REST controller

---

## Review Checklist by Layer

### Domain Layer

- [ ] No Spring/infrastructure imports
- [ ] No SM DTOs - only domain models
- [ ] Calculations extend existing `*CalculationAbstract`
- [ ] Value objects are immutable

### Application Layer

- [ ] Uses ports, not concrete adapters
- [ ] Commands/DTOs are immutable
- [ ] Proper validation before domain operations

### Web Client Adapter

- [ ] New fetchers extend `AbstractSecurityMasterFetcher` (not creating standalone implementations)
- [ ] Resilience4j annotations present (CircuitBreaker, Retry, Bulkhead)
- [ ] Fallback methods implemented
- [ ] Errors translated to domain exceptions
- [ ] No duplicate code across fetcher classes
- [ ] Batch methods available (no N+1)
- [ ] Stubs removed when real fetcher is implemented

### REST Adapter

- [ ] Request validation present
- [ ] No business logic
- [ ] Proper HTTP status codes
- [ ] No SM data exposed directly

---

## Review Output Format

```
## Code Review

### Critical (X issues)
1. **[File:Line]** Issue + fix

### High (X issues)
...

### Medium (X issues)
...

### Positive
- Good patterns observed
```

---

## Key Questions During Review

1. **Can these classes be abstracted?** → Look for duplicate code across similar classes
2. **Is there a hierarchy?** → Securities, SM fetchers should have abstract parents
3. **N+1 calls?** → Any SM fetch inside a loop?
4. **Resilience?** → CircuitBreaker, Retry, Bulkhead on external calls?
5. **Layer violations?** → Domain must be pure, adapters must not call each other
6. **Magic strings?** → Should be constants or enums
7. **Repeated code?** → Should be extracted to utility methods
