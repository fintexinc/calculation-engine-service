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

Calculation engine service: fetches data from Security Master (SM) via REST and GraphQL, caches in Redis, performs
financial calculations. **No database.**

---

## 1) CRITICAL: Think Hierarchy First

**Before writing ANY class, ask: "Can this be abstracted?"**

This codebase uses deep hierarchies. Example:

```
Security (abstract)
├── TradableSecurity (abstract)
│   ├── Etf
│   ├── Fund
│   ├── Stock
│   └── Index
└── ...
    └── ...
```

### Template Method Pattern

```java
// ONE abstract class with the algorithm
public abstract class SecurityDataFetcher<T extends Security> {

    public final T fetch(String id) {
        var rawData = fetchFromSM(id);      // Common
        validate(rawData);                   // Common
        return mapToSecurity(rawData);       // Abstract - each type implements
    }

    protected abstract T mapToSecurity(SMResponse data);
    protected abstract Class<T> getSecurityType();
}

// Subclasses ONLY implement what's different
public class EtfDataFetcher extends SecurityDataFetcher<Etf> {
    @Override
    protected Etf mapToSecurity(SMResponse data) {
        return Etf.builder()/* ETF-specific mapping */.build();
    }

    @Override
    protected Class<Etf> getSecurityType() { return Etf.class; }
}
```

### Strategy Pattern (for varying algorithms)

```java
public interface CalculationStrategy {
  boolean supports(SecurityType type);

  BigDecimal calculate(SecurityData data);
}

@Component
public class EtfCalculationStrategy implements CalculationStrategy {
  @Override
  public boolean supports(SecurityType type) {
    return type == SecurityType.ETF;
  }
  // ...
}

// Context selects strategy
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

**Red flags (DON'T DO):**

- 20 classes with identical method bodies
- Copy-pasting code between similar classes
- Generic subclass only changing the type parameter

**Existing abstract classes to extend:**

- `*CalculationAbstract` - calculations
- `Abstract*Endpoint` - GraphQL endpoints
- `MultipleCacheStorageAbstract` - cache storage

---

## 2) Module Placement & Ports

| Code                         | Module                 | Rule                               |
|------------------------------|------------------------|------------------------------------|
| Business logic, calculations | domain                 | No Spring, no adapters, no SM DTOs |
| Use cases, orchestration     | application            | Uses ports only                    |
| REST API                     | rest-adapter           | No business logic                  |
| SM GraphQL calls             | graphql-client-adapter | Implements output ports            |
| Redis cache                  | cache-adapter          | Implements cache ports             |

**Forbidden:** domain → Spring/adapters, adapter → another adapter

### Port Design

```java
// Output port (in api module) - abstracts external service
public interface SecurityDataRepository {
  Optional<SecurityData> findById(String securityId);

  List<SecurityData> findByIds(List<String> securityIds);  // Batch method!
}

// Adapter implements port (in graphql-client-adapter)
@Component
public class SMSecurityDataRepository implements SecurityDataRepository {
  // Implementation calls SM via GraphQL
}
```

---

## 3) External Calls (SM)

### Never call SM in loops (N+1)

```java
// BAD: N+1 calls
items.forEach(i -> smClient.fetch(i.getId()));

// GOOD: Batch
List<String> ids = items.stream().map(Item::getId).toList();
smClient.fetchBatch(ids);
```

### Always use Resilience4j

```java

@CircuitBreaker(name = "securityMaster", fallbackMethod = "fetchFallback")
@Retry(name = "securityMaster")
@Bulkhead(name = "securityMaster")
public SecurityData fetch(String id) {
  return graphqlClient.query(buildQuery(id));
}

public SecurityData fetchFallback(String id, Exception e) {
  log.warn("SM unavailable for {}, using cache fallback", id);
  return cacheStorage.get(id)
          .orElseThrow(() -> new SecurityDataUnavailableException(id, e));
}
```

### Configuration in application.yaml

```yaml
resilience4j:
  circuitbreaker:
    instances:
      securityMaster:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s

  retry:
    instances:
      securityMaster:
        maxAttempts: 3
        waitDuration: 1s
        retryExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException

  bulkhead:
    instances:
      securityMaster:
        maxConcurrentCalls: 20
```

No hardcoded timeouts, URLs, retry counts in code.

---

## 4) Caching (Redis)

### Cache-Aside Pattern

```java
public SecurityData getData(String id) {
  // 1. Check cache first
  return cacheStorage.get(id)
          .orElseGet(() -> {
            // 2. Fetch from SM on cache miss
            var data = smRepository.findById(id)
                    .orElseThrow(() -> new SecurityNotFoundException(id));
            // 3. Store in cache
            cacheStorage.put(id, data);
            return data;
          });
}
```

### Graceful Degradation

```java
public Optional<SecurityData> getFromCache(String id) {
  try {
    return cacheStorage.get(id);
  } catch (RedisConnectionException e) {
    log.warn("Redis unavailable, skipping cache for {}", id);
    return Optional.empty();  // Don't fail, just miss cache
  }
}
```

---

## 5) Null Safety

- Return `Optional<T>` for things that might not exist
- Validate SM data at adapter boundary:

```java
public SecurityData mapFromSM(SMResponse response) {
  Objects.requireNonNull(response, "SM response is null");

  return SecurityData.builder()
          .id(requireNonBlank(response.getId(), "Security ID"))
          .name(response.getName())
          .price(Optional.ofNullable(response.getPrice()).orElse(BigDecimal.ZERO))
          .build();
}
```

- Never return null collections - use `List.of()`

---

## 6) Exception Handling

### Translate at adapter boundary

```java
try{
        return smClient.query(request);
}catch(
GraphQLClientException e){
        if(e.

isNotFound()){
        throw new

SecurityNotFoundException(securityId);
    }
            throw new

SecurityDataUnavailableException(
        "Failed to fetch "+securityId +" from SM", e);
}
```

### Meaningful messages with context

```java
throw new CalculationException(String.format(
                "Failed to calculate returns for security %s: insufficient data (got %d, need %d)",
        securityId, actualCount, requiredCount));
```

---

## 7) Clean Code

- Max 5-7 dependencies per class (split if more)
- Max 3 nesting levels (use early returns)
- Use enums instead of strings/booleans for types
- **Extract strings into constants or enums** - no magic strings
- **Extract repeated code into utility methods** - same 3+ lines twice → create util

---

## 8) Before Writing Code

1. Does similar code exist? → Extend/reuse it
2. Can this be abstracted? → Create hierarchy with Template Method
3. Correct module? → Domain has no Spring
4. Batch SM calls? → No loops
5. Resilience configured? → CircuitBreaker, Retry, Bulkhead
6. Config in YAML? → No hardcoded values
