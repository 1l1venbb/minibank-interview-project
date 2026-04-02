# MiniBank -- Starter Project

## What's Pre-configured

The starter project has the following already set up — you can start implementing the API immediately without any additional configuration.

| Item | Detail |
|------|--------|
| Spring Boot 4.0.3 / Java 25 | Parent POM `spring-boot-starter-parent:4.0.3`, `java.version=25` |
| Spring MVC | `spring-boot-starter-webmvc` (renamed from `spring-boot-starter-web` in Spring Boot 4) |
| H2 Database | In-memory database `jdbc:h2:mem:minibank` with `ddl-auto: create-drop` — schema auto-created from JPA entities |
| H2 Console | `spring-boot-h2console` — accessible at `/h2-console` during development |
| Exchange Rates | `exchange-rates.yml` loaded via `spring.config.import` — mapped to `exchange.rates.*` properties |
| Actuator Health | `GET /actuator/health` responds `{"status":"UP"}` — used by the automated evaluator to check application readiness |
| Package Structure | Empty `controller/`, `model/`, `repository/`, `service/` packages with `.gitkeep` files — ready for your implementation |

---

## Getting Started

### Prerequisites

- Java 25
- Maven 3.9+

### Running the Application

```bash
cd minibank-starter
mvn spring-boot:run
```

Verify startup:

```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

---

## H2 Console

The in-memory H2 database is accessible during development at:

```
http://localhost:8080/h2-console
```

Connection settings:
- JDBC URL: `jdbc:h2:mem:minibank`
- Username: `sa`
- Password: (leave empty)

---

## Exchange Rates

Pinned currency exchange rates are loaded from `src/main/resources/exchange-rates.yml`. The rates in that file are fixed and will be used by the automated test suite — do not modify them.

---

## Project Structure

The starter project provides an empty package layout to help organise your implementation:

- `ro.axonsoft.eval.minibank` — root package with `MinibankApplication.java` (Spring Boot entry point — do not move or rename this class)
  - `controller/` — REST controllers for the API endpoints
  - `model/` — JPA entities and DTOs
  - `repository/` — Spring Data JPA repositories
  - `service/` — Business logic services

You are free to create additional packages as needed. The provided structure is a suggestion, not a constraint.

---

## Build Environment

Your submission is compiled and tested in an **offline** environment with no network access. The starter dependencies are always available. The following additional libraries are also pre-installed — you may add any of them to your `pom.xml` without specifying a version (managed by the Spring Boot 4.0.3 BOM unless noted):

**Spring Boot Starters**

| Artifact | Scope |
|----------|-------|
| `spring-boot-starter-test` | test |
| `spring-boot-starter-validation` | compile |
| `spring-boot-starter-webmvc` | compile |
| `spring-boot-starter-security` | compile |
| `spring-boot-starter-cache` | compile |
| `spring-boot-starter-aop` | compile |
| `spring-boot-devtools` | provided |

**Utility Libraries**

| Library | Maven Coordinates | Version |
|---------|-------------------|---------|
| Apache Commons Lang | `org.apache.commons:commons-lang3` | BOM |
| Apache Commons IO | `commons-io:commons-io` | 2.18.0 |
| Apache Commons Text | `org.apache.commons:commons-text` | 1.13.0 |
| Apache Commons Collections | `org.apache.commons:commons-collections4` | 4.5.0 |
| Apache Commons Validator | `commons-validator:commons-validator` | 1.9.0 |
| Google Guava | `com.google.guava:guava` | 33.4.0-jre |
| Gson | `com.google.code.gson:gson` | 2.11.0 |

**Code Generation & Mapping**

| Library | Maven Coordinates | Version |
|---------|-------------------|---------|
| Lombok | `org.projectlombok:lombok` | BOM |
| MapStruct | `org.mapstruct:mapstruct` | 1.6.3 |
| MapStruct Processor | `org.mapstruct:mapstruct-processor` | 1.6.3 |
| ModelMapper | `org.modelmapper:modelmapper` | 3.2.2 |

**Validation**

| Library | Maven Coordinates | Version |
|---------|-------------------|---------|
| Jakarta Validation API | `jakarta.validation:jakarta.validation-api` | BOM |
| Hibernate Validator | `org.hibernate.validator:hibernate-validator` | BOM |

> **Important:** The build environment has no network access. If you add a dependency not listed above, your build will fail and functional tests will score zero.

---

## Submission Instructions

Your submission will be evaluated automatically by a test suite.

Submit your implementation as a ZIP archive through the evaluation platform before the deadline shown on your dashboard.

You are encouraged to use any tools, including AI assistants.

---

## Full Specification

For the complete API specification and business rules, refer to the challenge page on the evaluation platform.
