# Contributing to Fintex Calculation Engine Service

Thank you for contributing to the Fintex Calculation Engine Service! This document provides guidelines and best
practices for contributing to this project.

## Table of Contents

- [Code Style Guidelines](#code-style-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Development Workflow](#development-workflow)

## Code Style Guidelines

### Use Optional or the ternary operator for Null Safety

Prefer using `Optional` instead of null checks to handle nullable variables and transform types.
The exception is the ternary operator as it's short and concise.

**❌ Bad:**

```java
public String getSecurityName(Security security) {
    if (security == null) {
        return "Unknown";
    }
    if (security.getName() == null) {
        return "Unknown";
    }
    return security.getName();
}
```

**✅ Good:**

```java
public String getSecurityName(Security security) {
    return Optional.ofNullable(security)
        .map(Security::getName)
        .orElse("Unknown");
}
```

**✅ Good:**

```java
Optional.ofNullable(user)
    .

map(User::getEmail)
    .

filter(email ->email.

contains("@"))
        .

orElseThrow(() ->new

InvalidEmailException());
```

**✅ Good:**

```java
return entity ==null?new

Entity() :entity;
```

### Think Hierarchy First

Before writing any class, ask: "Can this be abstracted?"

This codebase uses deep hierarchies. When you see similar classes (e.g., EtfEndpoint, FundEndpoint, StockEndpoint), use
the Template Method pattern:

**❌ Bad:** 20 classes with identical method bodies

```java
public class EtfEndpoint extends AbstractEndpoint<EtfResponse> {
    public EtfResponse fetch(String id) {
        return client.query(id, EtfResponse.class);  // Same in ALL classes!
    }
}
```

**✅ Good:** Common logic in abstract class, subclasses only provide type-specific details

```java
public abstract class AbstractEndpoint<T> {
    protected abstract Class<T> getResponseType();

    public T fetch(String id) {  // Single implementation
        return client.query(id, getResponseType());
    }
}
```

### External Service Calls (Security Master)

This service fetches data from Security Master (SM) via REST/GraphQL. All external calls must:

1. **Never call SM in loops (N+1 problem)**
   ```java
   // ❌ Bad
   items.forEach(i -> smClient.fetch(i.getId()));

   // ✅ Good
   smClient.fetchBatch(ids);
   ```

2. **Use Resilience4j annotations**
   ```java
   @CircuitBreaker(name = "securityMaster", fallbackMethod = "fallback")
   @Retry(name = "securityMaster")
   @Bulkhead(name = "securityMaster")
   public Data fetch(String id) { }
   ```

3. **Configuration in application.yaml** - no hardcoded timeouts, URLs, or retry counts in code

### Extract Constants and Utilities

- **Extract strings into constants or enums** - no magic strings in code
- **Extract repeated code into utility methods** - if you write the same 3+ lines twice, create a util

### Code Formatting

All Java code must follow the project's formatting rules defined in `eclipse-java-formatter.xml`. We use Spotless with
the Eclipse formatter. Before creating a pull request:

```bash
# Format all code
mvn spotless:apply

# Check formatting without applying changes
mvn spotless:check
```

The CI pipeline includes a formatting check that will fail if code is not properly formatted.

**Formatting guidelines:**

- Use 2 spaces for indentation
- Maximum line length: 120 characters
- Braces on same line (end_of_line style)
- Each enum constant on a new line
- Simple if statements can be on one line: `if (this == o) return true;`
- Method chains preserve existing formatting
- Use Java 21 features where appropriate (records, pattern matching, etc.)
- Follow hexagonal architecture patterns established in the codebase

### Test Naming Convention

Test methods must follow the `shouldDoSomething_whenCondition` naming pattern. The name should clearly describe the
expected behavior and the condition that triggers it.

**Examples:**

```java
@Test
void shouldCalculateReturns_whenSecurityDataExists() { ... }

@Test
void shouldThrowException_whenSecurityNotFound() { ... }

@Test
void shouldReturnCachedData_whenCacheHit() { ... }

@Test
void shouldFetchFromSM_whenCacheMiss() { ... }
```

### Avoid Duplicate Tests

- Use `@ParameterizedTest` when same logic with different inputs
- Use abstract test classes with Template Method pattern when testing similar implementations
- Before writing a new test, check if a similar test exists that can be extended

## Commit Message Guidelines

### Conventional Commits

We follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification for all commit
messages.

**Format:**

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code changes that neither fix a bug nor add a feature
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Changes to build process, dependencies, or tooling
- `ci`: Changes to CI/CD configuration

### Ticket References

All commits must include a ticket reference in the footer using one of these formats:

- `refs: <ticket-number>` - For general references
- `fixes: <ticket-number>` - For bug fixes
- `closes: <ticket-number>` - For feature completion

You can include several ticket numbers if a commit relates to them.

**Examples:**

```
feat(domain): add Sharpe ratio calculation

Implement Sharpe ratio calculation extending PeriodCalculationAbstract.
Uses existing template method pattern for consistency.

refs: CE-123
```

```
fix(graphql-adapter): add circuit breaker to SM endpoint

Added Resilience4j annotations to prevent cascading failures
when Security Master is unavailable.

fixes: CE-456, CE-457
```

```
chore(deps): update Spring Boot to 3.5.8

refs: CE-789
```

## Pull Request Process

### Branch Strategy

We use a rebase and fast-forward merge approach to maintain a linear git history.

1. **Create a feature branch from main:**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feat/your-feature-name
   ```

2. **Make your changes and commit following the guidelines above**

3. **Before creating a PR, rebase onto main:**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

4. **If conflicts occur, resolve them and continue:**
   ```bash
   # Resolve conflicts in your editor
   git add .
   git rebase --continue
   ```

5. **Force push your rebased branch (only for feature branches):**
   ```bash
   git push origin feat/your-feature-name --force-with-lease
   ```

### Commit Squashing

Each PR should contain **1-2 main commits** maximum. If you have multiple commits, squash them before creating the PR:

```bash
# Interactive rebase to squash last N commits
git rebase -i HEAD~N

# In the editor, change 'pick' to 'squash' (or 's') for commits you want to combine
# Save and close the editor
# Edit the combined commit message
```

**Example scenario:**

You have 5 commits:

```
* feat: add calculation endpoint (latest)
* fix: typo in formula
* refactor: extract method
* test: add unit tests
* feat: initial implementation
```

Squash them into 1-2 meaningful commits:

```
* feat(domain): add Sharpe ratio calculation with tests
```

### Handling Review Feedback

**IMPORTANT:** Maintain the 1-2 commit limit even after fixing review comments.

When fixing code review feedback:

1. **Make the requested changes**

2. **Amend the existing commit(s)** instead of creating new "fix review comments" commits:
   ```bash
   # Make your changes
   git add .
   git commit --amend --no-edit  # Keep the same commit message

   # Or edit the commit message if needed
   git commit --amend
   ```

3. **Force push the updated branch:**
   ```bash
   git push origin feat/your-feature-name --force-with-lease
   ```

**Why this matters:**

- Prevents accidentally polluting the main branch with "fix review comments" or "address feedback" commits
- Maintains a clean, linear git history
- Each commit in main represents a complete, meaningful change

Developers can forget to squash commits before merge.

### Pull Request Checklist

Before creating a pull request, ensure:

- [ ] Code is formatted: `mvn spotless:apply`
- [ ] All tests pass: `mvn test`
- [ ] Branch is rebased onto latest main
- [ ] Commits follow Conventional Commits format
- [ ] Commits include ticket references
- [ ] PR contains 1-2 main commits
- [ ] PR description explains what and why (not how)
- [ ] No N+1 external service calls
- [ ] Resilience4j annotations on SM calls
- [ ] No hardcoded configuration values

## Development Workflow

### Running the Application

```bash
# Build entire project
mvn clean install

# Run with specific profile
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=devlocal

# Run tests
mvn test
```

### Module Structure (Hexagonal Architecture)

| Module                   | Purpose                                    |
|--------------------------|--------------------------------------------|
| `domain`                 | Business logic, calculations (no Spring)   |
| `application`            | Use cases, orchestration (uses ports only) |
| `api`                    | Port interfaces                            |
| `rest-adapter`           | REST API endpoints                         |
| `graphql-client-adapter` | Security Master GraphQL client             |
| `cache-adapter`          | Redis caching                              |
| `bootstrap`              | Application startup, wiring                |

**Architecture rules:**

- Domain must not import Spring or adapters
- Adapters must not call each other directly
- Application layer uses ports, not concrete adapters

### Code Review

All pull requests require:

- Approval from designated code owners (see CODEOWNERS file)
- Passing CI checks (tests, formatting, build)
- No unresolved conversations

#### Conversation Resolution Guidelines

**IMPORTANT:** Do not resolve conversations in advance.

Follow these rules when handling PR conversations:

1. **Only resolve a conversation if:**
    - You have implemented the requested change exactly as specified in the comment
    - The reviewer has explicitly approved your alternative approach
    - The discussion has reached a mutual agreement

2. **Do not resolve a conversation if:**
    - You're still waiting for reviewer feedback
    - You've implemented a different solution than requested (discuss first)
    - The conversation contains a question that hasn't been answered
    - You're unsure about the requested change

3. **When in doubt:**
    - Reply to the conversation explaining your approach
    - Ask for clarification if the feedback is unclear
    - Let the reviewer resolve the conversation after verifying your changes

**Why this matters:**

- Ensures all feedback is properly addressed
- Prevents miscommunication between author and reviewer
- Makes it clear which items still need attention
- Maintains accountability in the review process

### Merge Process

Once approved:

1. Ensure all CI checks pass
2. Maintainer will perform a fast-forward merge to main
3. Feature branch **will be deleted** after merge

## Questions?

If you have questions about these guidelines, please reach out to the maintainers or open a discussion issue.
