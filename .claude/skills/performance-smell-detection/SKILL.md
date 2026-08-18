---
name: performance-smell-detection
description: Detect performance smells across the stack — algorithm complexity (N^2), collections/streams/boxing, database (missing indexes, N+1, over-joining, missed pagination), interservice call fan-out, memory (unbounded loads, unclosed resources, caches without eviction, leaked connections), concurrency (deadlocks, thread starvation, lock scope, optimistic locking under contention), and logging. Awareness not absolutes — measure before optimizing. Use when reviewing performance-critical code or investigating latency, memory, or throughput problems.
---

# Performance Smell Detection

Notice **potential** performance smells across the stack and know the standard fix. This is awareness, not a mandate to change code.

**Golden rule:** measure first (JMH / profiler / prod metrics / query plans), optimize only proven hot paths, never trade away readability for a micro-gain. "Looks slow" is not a measurement.

**This repo's reality:** no database — the dominant cost is **interservice calls** to MIC and Bank of Canada, plus in-memory calculation over holdings. The DB and concurrency sections below still apply the moment that surface is introduced.

## Categories at a glance

| Area | High-severity smell | Standard fix |
|------|---------------------|--------------|
| Algorithms | N² nested scans, repeated linear lookups | Precompute `Map`/`Set`; sort once → N·log N |
| Collections/JVM | regex compiled in loop, `List.contains` in loop, boxing in tight loop | Hoist `Pattern`; use `Set`; primitive streams |
| Database | N+1 queries, missing index, over-joining, no pagination | Fetch-join/batch; index WHERE/JOIN/ORDER BY cols; project columns; paginate |
| Interservice | call inside a loop (N+1 across services), chatty sequential calls, no caching | Batch/aggregate; parallelize independent calls; cache stable data |
| Memory | unbounded load, unclosed resource, cache without eviction, leaked connection | Paginate/stream; try-with-resources; bounded cache; pool + return |
| Concurrency | deadlock, thread starvation, oversized `synchronized`, optimistic lock under contention | Order locks; bound pools; narrow critical section; pessimistic/redesign |
| Logging | string-concatenated stack trace, logging in hot loops | Parameterized SLF4J + throwable arg; guard/remove |

## 1. Algorithms & complexity
Most hot N² can drop to N·log N or N. Look for a nested loop or a linear lookup repeated per element.

```java
// 🔴 O(n·m): membership re-scanned every iteration
for (Holding h : holdings) if (excludedList.contains(h.id())) skip(h);
// ✅ O(n): precompute once
Set<String> excluded = new HashSet<>(excludedList);
for (Holding h : holdings) if (excluded.contains(h.id())) skip(h);
```
Also: sort once instead of re-sorting in a loop; cache an expensive pure result instead of recomputing.

## 2. Collections, streams & JVM
- **Regex** compiled in a loop → hoist to `static final Pattern`. Always worth it.
- **`List.contains` in a loop** → use a `Set` (O(1)).
- **Boxing** in a tight loop / reduction → primitive streams (`mapToInt`/`mapToLong`).
- **`String +=` in a loop** → `StringBuilder` / `String.join`. (Simple non-loop concatenation is fine since Java 9.)
- **Missing capacity hint** (`new ArrayList<>(size)`) → free win only when size is known.
- **Streams**: prefer for readability (CLAUDE.md mandates Stream API); only rewrite to loops when a profiler flags a >100k-iteration hot path. **Parallel streams** only for CPU-bound work on large collections, never with shared mutable state.
- **Never put a blocking call in a `parallelStream()`** — external service (MIC/BoC), DB, or any I/O. Parallel streams run on the shared **common `ForkJoinPool`** (sized to CPU cores), so a blocking task there starves *every other* parallel stream in the JVM and gives none of the throughput you actually want for I/O.

```java
// 🔴 blocks common ForkJoinPool threads, starves the whole JVM's parallel work
List<Result> r = holdings.parallelStream().map(h -> smsClient.fetch(h.id())).toList();
// ✅ I/O fan-out on a dedicated executor / virtual threads
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
  List<Future<Result>> fs = holdings.stream().map(h -> exec.submit(() -> smsClient.fetch(h.id()))).toList();
  // ... collect fs
}
```
parallelStream is for **CPU-bound** work only. (Better still for MIC: batch into one call — see §4.)

## 3. Database (when a DB is added)
- **N+1 queries** — a query per row inside a loop. Fix with a fetch-join or a single batched `IN (...)` query.
- **Missing / bad indexes** — index columns used in `WHERE`, `JOIN`, `ORDER BY`. Composite-index column order matters (most selective / equality first, range last). Don't wrap an indexed column in a function (`WHERE lower(x)=…` defeats the index). Verify with `EXPLAIN`.
- **Over-joining / `SELECT *`** — project only needed columns; question every join; a report joining 8 tables usually wants a purpose-built view or projection.
- **Missed pagination** — never return an unbounded result set; page or stream it.
- **Bad query building** — parameterize (no string concat → also SQL-injection); don't build queries inside a loop; for deep pages prefer keyset pagination over large `OFFSET`.

## 4. Interservice communication (critical here)
**N+1 fan-out and missing Resilience4j/timeouts on MIC/BoC calls are owned by `code-reviewer` and `coder` §4 — not restated here.** What this skill adds on top:
- **Chatty sequential calls** — independent calls awaited one-by-one. Parallelize with virtual threads / `CompletableFuture` on a dedicated executor — **not `parallelStream()`** (blocking on the common ForkJoinPool, see §2).
- **No caching of stable upstream data** — repeatedly fetching data that rarely changes → cache with a TTL (see `cache-adapter`).

## 5. Memory
- **Unbounded object load** — materializing huge collections (no pagination) → stream/paginate; process in batches.
- **Unclosed resources** — streams, readers, `InputStream`, JDBC handles → **try-with-resources**; a `Stream` over I/O (e.g. `Files.lines`) must be closed.
- **Cache without eviction** — a map *used as a cache* that only grows is a leak → bounded size + TTL eviction (e.g. Caffeine). (A plain `ConcurrentHashMap` is fine for non-cache state; only unbounded caching is the smell.)
- **Leaked / idle connections** — not returning pooled connections, oversized idle pools → size the pool, always release; monitor idle count.
- **Exception handling that retains memory** — holding large objects in long-lived fields, or swallowing exceptions so cleanup is skipped → free references, clean up in `finally`/try-with-resources.

## 6. Concurrency (when shared mutable state is added)
- **Deadlock** — two locks acquired in different orders → establish one global lock ordering; keep critical sections short.
- **Thread starvation** — blocking I/O on a small/shared pool (or the common ForkJoinPool) → bounded dedicated pools; virtual threads for blocking I/O.
- **Oversized `synchronized` block** — locking more than the invariant needs → narrow the critical section to the shared-state mutation only.
- **`synchronized` method vs block** — a synchronized *method* locks the whole object for its whole body; prefer a `synchronized(lock)` block over a private lock object guarding just the shared field.
- **Optimistic locking under high contention** — version-check retries storm when writes collide often → use pessimistic locking or redesign to reduce contention (partition, queue, atomic ops). Optimistic locking suits low-contention only.

## 7. Logging
```java
// 🔴 builds a useless string, drops the real stack trace, cost even when disabled
log.error("error" + exception.getStackTrace().toString());
// ✅ parameterized message + throwable as last arg → SLF4J renders the full stack trace
log.error("Calculation failed for metric {}", metricName, exception);
```
- Pass the `Throwable` as the last argument — never `+ e.getStackTrace()` or `+ e.toString()`.
- Use parameterized `{}` logging, not string concatenation (no cost when the level is disabled).
- Don't log inside hot loops; guard expensive debug messages with `log.isDebugEnabled()` only when building the argument is itself costly.

## When NOT to optimize
Setup/config/admin code · no measured problem · readability would suffer · data small enough to process in microseconds.

## Quick grep for a review pass
```bash
grep -rn "\.matches(\|\.split("        --include="*.java"   # regex compiled inline
grep -rn "findAll()"                    --include="*.java"   # unbounded queries/loads
grep -rn "new ArrayList<>()"            --include="*.java"   # capacity hints where size known
grep -rn "getStackTrace()\|+ *e\.toString()" --include="*.java" # bad stack-trace logging
grep -rn "synchronized "                --include="*.java"   # review lock scope
grep -rn "new ConcurrentHashMap<>()"    --include="*.java"   # inspect ONLY those used as unbounded caches
```

## Related
- **`code-reviewer`** — external-call fan-out, N+1, and Resilience4j on MIC/BoC calls
- **`clean-code`** — premature optimization is an antipattern: measure first
