# Java Development Guidelines

## Language & Runtime
- Java 25 LTS — use modern features: records, sealed classes, pattern matching (`switch` expressions, record patterns), text blocks, unnamed variables
- Prefer `var` for local variables when the type is obvious from the right-hand side
- Use `String::formatted` or `MessageFormat` over string concatenation in non-trivial cases

## Project Structure
- Follow standard Maven layout: `src/main/java`, `src/main/resources`, `src/test/java`
- One public class per file, filename matches class name
- Package-by-feature over package-by-layer when creating new modules

## Coding Standards
- All fields `private final` unless mutability is required
- Use records for DTOs, value objects, and configuration bindings
- Prefer `List.of()`, `Map.of()`, `Set.of()` for immutable collections
- Use `Optional<T>` as return type — never as field, parameter, or collection element
- No raw types — always parameterize generics
- Use `sealed` interfaces/classes to model closed type hierarchies
- Prefer composition over inheritance

## Null Safety
- Never return `null` — use `Optional`, empty collections, or domain-specific defaults
- Annotate parameters and return types with `@Nullable`/`@NonNull` from `org.jspecify` when intent isn't obvious
- Use `Objects.requireNonNull()` at public API boundaries

## Exception Handling
- Throw specific exceptions, not `RuntimeException` or `Exception`
- Catch specific exceptions — never catch `Throwable` or `Exception` broadly
- Use custom exceptions extending `RuntimeException` for business rule violations
- Include context in exception messages: what failed, which entity, what ID

## Logging
- Use SLF4J via `LoggerFactory.getLogger(ClassName.class)`
- Never `System.out.println` or `System.err.println`
- Use parameterized messages: `log.info("Processing order {}", orderId)` — no string concatenation
- Log at appropriate levels: `ERROR` for failures needing attention, `WARN` for recoverable issues, `INFO` for business events, `DEBUG` for troubleshooting

## Testing
- JUnit 5 (`org.junit.jupiter.api`) exclusively — no JUnit 4
- Use `@DisplayName` for readable test descriptions
- Follow Arrange-Act-Assert pattern
- Use AssertJ for fluent, readable assertions
- Mock external dependencies with Mockito — never mock the class under test
- Name test methods: `shouldDoExpectedBehavior_whenCondition`
- Aim for one assertion concept per test

## Spring Framework
- Constructor injection only — no `@Autowired` on fields
- `@Transactional` on service methods, not on repositories or controllers
- Use `@ConfigurationProperties` records for type-safe configuration
- `@RestControllerAdvice` with `ProblemDetail` (RFC 9457) for error handling
- Validate at controller boundary: `@Valid`, `@NotNull`, `@NotBlank`, `@Size`
- Keep controllers thin — delegate to service layer
- Use `@WebMvcTest` for controller tests, `@DataJpaTest` for repository tests

## Date & Time
- `java.time` API only — never `java.util.Date`, `Calendar`, or `java.sql.Timestamp`
- Use `Instant` for timestamps, `LocalDate` for dates without time, `ZonedDateTime` when timezone matters
- Store as UTC, convert to user timezone at the presentation layer

## Security
- Never hardcode secrets, passwords, API keys, or tokens
- Use environment variables (`${PLACEHOLDER}`) or external secret stores
- Validate and sanitize all external input
- Use parameterized queries — never concatenate SQL strings

## Dependencies
- Manage versions through Spring Boot BOM — do not override BOM-managed versions without reason
- Keep dependencies minimal — remove unused libraries
- Prefer well-maintained libraries with active communities

## Build
- Build: `mvn clean verify`
- Run: `mvn spring-boot:run`
- All code must compile and pass tests before considering work complete
