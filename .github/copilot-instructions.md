# Project Guidelines — Sector 7G Safety Ledger

## Target Stack
- Java 25 (LTS)
- Spring Boot 4.x / Spring Framework 7.x
- Maven build

## Build and Test
- Build: `mvn clean verify`
- Run: `mvn spring-boot:run`
- All changes must compile and pass tests before completion

## Namespace Migration
- Replace ALL `javax.*` imports with `jakarta.*` (e.g. `javax.persistence` → `jakarta.persistence`, `javax.servlet` → `jakarta.servlet`)
- Never mix javax and jakarta namespaces in the same codebase

## Spring Boot 4 / Spring Framework 7 Rules
- SpringFox is dead — replace with `springdoc-openapi-starter-webmvc-ui`
- Remove `@EnableSwagger2` and the `Docket` bean; use springdoc auto-configuration
- Use constructor injection, not field-level `@Autowired`
- JUnit 5 only (`org.junit.jupiter.api.Test`), never JUnit 4 (`org.junit.Test`)

## Security — Hard Rules
- NEVER hardcode secrets, passwords, API keys, or tokens in source code
- NEVER put credentials in properties files in plaintext
- `SecretConstants.java` must be deleted — do not reference or recreate it
- Use `${PLACEHOLDER}` environment variables or Azure Key Vault property source for all secrets

## Dependency Policy
- Use Spring Boot BOM for transitive version management
- Upgrade vulnerable libraries: commons-text, commons-collections, guava to latest stable
- Remove any dependency that is abandoned or incompatible with Spring Boot 4

## Code Conventions
- Package structure: `com.springfield.plant.*` (preserve existing)
- Prefer Java 25 features where appropriate: records, pattern matching, text blocks, sealed classes

## Best Practices
- Favor immutability: use `final` fields, unmodifiable collections, and records for data carriers
- Return `Optional<T>` instead of `null` from service methods
- Use `@Transactional` at the service layer, not on repositories or controllers
- Keep controllers thin — delegate business logic to services
- Use `@ConfigurationProperties` with records for type-safe config binding
- Validate inputs at the controller boundary with Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- Write tests first: unit tests for services, `@WebMvcTest` for controllers, `@DataJpaTest` for repositories
- Log with SLF4J (`LoggerFactory`), never `System.out.println`
- Handle exceptions with `@RestControllerAdvice` and `ProblemDetail` (RFC 9457), not per-controller catch blocks
- Use `java.time` API exclusively — never `java.util.Date` or `java.sql.Timestamp`
