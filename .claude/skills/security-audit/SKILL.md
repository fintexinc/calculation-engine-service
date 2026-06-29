---
name: security-audit
description: Java security checklist covering OWASP Top 10, input validation, injection prevention, and secure coding. Works with Spring, Quarkus, Jakarta EE, and plain Java. Use when reviewing code security, before releases, or when user asks about vulnerabilities.
---

# Security Audit

OWASP-informed checklist for Java services.

## Fit for this repo first
This is a **headless analytics backend**: no end-user auth, no user DB, no server-rendered HTML. It has one REST endpoint and makes **outbound** calls to SMS and Bank of Canada. So the security surface that actually matters here is, in order:

1. **Input validation at the boundary** — validate every request field and every value SMS returns.
2. **Outbound-call safety (SSRF)** — SMS/Bank-of-Canada URLs come from config, not request input; never build them from untrusted data.
3. **Secrets** — no hardcoded credentials; config references env vars only.
4. **No internal leakage** — error responses and logs must not expose stack traces, secrets, or sensitive payloads.
5. **Safe deserialization** — Jackson only, no polymorphic default typing on untrusted input.

The auth/session/XSS material further down is the general checklist — apply it only if that surface is actually introduced.

## OWASP Top 10 quick reference

| # | Risk | Java mitigation |
|---|------|-----------------|
| A01 | Broken Access Control | Deny by default, check at service layer |
| A02 | Cryptographic Failures | Strong algorithms, no hardcoded secrets |
| A03 | Injection | Parameterized queries, allowlist input validation |
| A04 | Insecure Design | Secure defaults, threat modeling |
| A05 | Misconfiguration | Disable debug in prod, secure headers |
| A06 | Vulnerable Components | Dependency scanning, keep current |
| A07 | Auth Failures | Strong hashing, MFA, session mgmt |
| A08 | Data Integrity | Verify signatures, safe deserialization |
| A09 | Logging Failures | Log security events, never sensitive data |
| A10 | SSRF | Validate/allowlist outbound URLs |

## Input validation (the primary control here)
Validate at the boundary with Bean Validation (`@Valid` + `@NotNull`/`@Size`/`@Pattern`) on request DTOs, and with `Objects.requireNonNull` on SMS responses (CLAUDE.md: "validate SMS data at the adapter boundary").

**Allowlist, not blocklist** — permit known-good, don't chase bad patterns:
```java
private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");
if (!SAFE_ID.matcher(input).matches()) throw new ValidationException("Invalid identifier");
```

## SSRF / outbound calls
Base URLs for SMS and Bank of Canada must come from configuration, never from request payloads. If any path/host segment is ever derived from input, validate against an allowlist of hosts. Keep Resilience4j on these calls (CLAUDE.md) — it also bounds abuse.

## Injection (if any query/native call is added)
Always parameterize — JPA named params, Criteria API, or `PreparedStatement`. **Never** string-concatenate a value into a query. This applies even to logging/filtering DSLs.

## Secrets management
```java
// ❌ hardcoded
private static final String API_KEY = "sk-123...";
// ✅ env-var backed config
@Value("${sms.api-key}") private String apiKey;   // application.yml: ${SMS_API_KEY}
```
`.gitignore`: `.env`, `*.pem`, `*.key`, `*secret*`, `application-local.yml`. Never commit real credentials.

## No internal leakage
```java
// ✅ generic client message, full detail in logs
@ExceptionHandler(Exception.class)
ResponseEntity<ErrorResponse> handle(Exception ex) {
    log.error("Unexpected error", ex);
    return ResponseEntity.status(500).body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected error"));
}
```
Logging: log security-relevant events (failed validation, upstream auth failures) but **never** log secrets, credentials, or full sensitive payloads.

## Safe deserialization
JSON via Jackson only; never `ObjectInputStream` on untrusted bytes. Do **not** enable `activateDefaultTyping` on untrusted input (gadget-chain RCE). `FAIL_ON_UNKNOWN_PROPERTIES=false` is fine.

## Dependencies
`mvn dependency-check:check` (fail build on high CVSS); keep dependencies current.

## General checklist (apply the parts that fit)
- [ ] Request + SMS data validated (allowlist patterns, `requireNonNull` at boundary)
- [ ] Outbound URLs from config, not input; Resilience4j present
- [ ] No hardcoded secrets; config uses env vars
- [ ] Error responses & logs leak no stack traces / secrets / payloads
- [ ] Deserialization is Jackson without default typing
- [ ] No SQL/native concatenation
- [ ] Dependencies scanned, no known high-severity CVEs
- [ ] Debug/dev features disabled in prod
- [ ] *(If auth/sessions/HTML added)* HTTPS enforced, security headers set, passwords hashed with BCrypt/Argon2, CSRF handled, output encoded

## Related
- **`code-reviewer`** — general review pass
