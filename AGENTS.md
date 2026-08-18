# Working in this repository

Java 21, Spring Boot 3.4, Maven multi-module (hexagonal). Build it with the
wrapper — `./mvnw` — so the Maven version is the pinned one.

## Formatting is enforced by the build

`spotless:check` is bound to the build lifecycle, so **badly formatted code
fails the build**, not just a linter. Before you finish a change:

```
./mvnw -B spotless:apply
```

The rules come from `eclipse-java-formatter.xml` and `eclipse.importorder` at
the repository root. Unused imports are removed automatically.

## The commands that gate a change

The same three the pipeline runs, in order:

```
./mvnw -B spotless:check          # formatting
./mvnw -B -DskipTests compile     # compiles on Java 21
./mvnw -B test                    # unit tests (surefire)
./mvnw -B -DskipTests package     # the runnable jar, at bootstrap/target/
```

The service jar is produced by the `bootstrap` module — the other modules are
libraries and produce no runnable artifact.

## Running it

Port 8181 (`dev` profile). The service needs the Security Master (`SM_REST_BASE_URL`)
to answer real calculations, and readiness is gated on it: with SMS
unreachable, the process starts and `/actuator/health/liveness` is UP while
readiness stays DOWN.
