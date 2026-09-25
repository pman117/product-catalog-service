# Product Catalog Service

[![CI](https://github.com/pman117/product-catalog-service/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/pman117/product-catalog-service/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.12-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-Kotlin_DSL-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-1.20.3-291A3F?logo=docker&logoColor=white)](https://testcontainers.com/)
[![TDD](https://img.shields.io/badge/TDD-test--first-brightgreen)]()

A production-grade **product catalog microservice** built with strict
Test-Driven Development. Every layer — Domain, Persistence, Service, Web —
was built red → green → refactor, each with a test type chosen for that
layer specifically.

> **Why this repo exists:** to demonstrate production engineering judgment,
> not framework familiarity. Every architectural decision below has a stated
> tradeoff and a rejected alternative.

---

## Tech Stack

| Concern | Technology | Why This Choice |
|---|---|---|
| Language | Java 21 (LTS) | Current LTS — records, pattern matching, virtual threads |
| Framework | Spring Boot 3.3.12 | Jakarta EE 10 namespace |
| Persistence | `NamedParameterJdbcTemplate` | Explicit SQL — `DECIMAL(65,30)` precision (ADR-1) |
| Migrations | Flyway 10.x | Version-controlled, auditable, same files everywhere |
| DB (prod) | MySQL 8.x | Production target |
| DB (integration tests) | Testcontainers MySQL 8.1 | Real engine, not an approximation (ADR-2) |
| Build | Gradle 8.14.2 (Kotlin DSL) | 4 separate test source sets |
| Unit tests | JUnit 5 + Mockito | `@ExtendWith(MockitoExtension.class)` |
| Controller tests | `@WebMvcTest` + MockMvc + `@MockBean` | HTTP contract slice — no DB |
| Integration tests | Testcontainers + `@DynamicPropertySource` | Authoritative |
| Validation | Jakarta Bean Validation | `@Valid` on `@RequestBody` |
| Error handling | `@RestControllerAdvice` | Central domain-exception → HTTP mapping |
| CI | GitHub Actions | Compile all source sets + test on every push |
| Containerization | Docker + Docker Compose | Local stack parity |

---

## Architecture

```
HTTP Client  (Postman / REST Assured / browser)
      │
      ▼
ProductController                      @RestController
      │  @Valid @RequestBody                 ← validation at the edge
      │  @ResponseStatus(CREATED / NO_CONTENT)
      │  GlobalExceptionHandler          @RestControllerAdvice
      │     ProductNotFoundException → 404
      │     DuplicateSkuException     → 409
      │     MethodArgumentNotValid    → 400
      ▼
ProductService                         @Service, @Transactional
      │  duplicate check before insert
      │  Optional.empty() → ProductNotFoundException  (.orElseThrow)
      ▼
DaoProductInterface                    (interface — swappable)
      ▼
DaoProductImplementationJdbc           @Repository
      │  NamedParameterJdbcTemplate, named SQL params
      │  static final RowMapper<Product>  — thread-safe, one instance
      │  InitializingBean.afterPropertiesSet() — fail-fast null guard
      ▼
MySQL 8.x    product.price DECIMAL(65,30)
      ▲
Flyway  V1__create_product_table.sql
```

---

## Architectural Decisions

### ADR-1 — `NamedParameterJdbcTemplate` over Spring Data JPA
`price` is `DECIMAL(65,30)`. IEEE-754 `double` is a binary fraction — `9.99`
becomes `9.98999…`. Hibernate can silently downcast `BigDecimal` during type
mapping unless precision and scale are declared exactly. That failure mode is
not a compile error; it is silent data corruption. Explicit JDBC with
`getBigDecimal()` guarantees an exact chain from column to HTTP response.

**Tradeoff:** hand-written SQL, no derived queries. Mitigated by
`DaoProductInterface` in front of the implementation, so swapping to JPA is a
wiring change rather than a rewrite.

### ADR-2 — Testcontainers MySQL 8.1 over H2
H2 in MySQL-compatibility mode is an approximation: different engine,
different strict-mode enforcement, different `DECIMAL` handling. Tests that
pass against an approximation prove less than they appear to. Testcontainers
starts real MySQL 8.1 per test class, wired via `@DynamicPropertySource`,
with Flyway running the same migration used in production.

**Tradeoff:** 5–15 s startup and a Docker dependency. Contained by isolating
these tests in their own source set, so `./gradlew test` stays fast.

### ADR-3 — `@Transactional` at the service layer only
The transaction boundary is the business-operation boundary. `createProduct`
= duplicate-check + insert, and those must be atomic. The DAO is a persistence
concern; the controller is an HTTP concern. Neither owns transaction lifecycle.

### ADR-4 — `Optional` on the DAO, `Product` on the service
The DAO cannot know whether "not found" is an error, so it returns
`Optional<Product>`. The service does know: `getProduct` either returns a
product or throws `ProductNotFoundException`. `.orElseThrow()` performs that
translation in one DB call with the correct exception and no `.get()`.

### ADR-5 — Domain exceptions, never persistence exceptions, across layers
Letting `DataIntegrityViolationException` escape upward would couple the
service and web layers to persistence internals. `GlobalExceptionHandler`
maps domain exceptions to status codes, so no controller ever sets one.

---

## Test Strategy

| Tier | Source set | Stack | Scope | Typical time |
|---|---|---|---|---|
| Unit | `src/test` | JUnit 5 + Mockito | Domain invariants, service logic | ~50 ms |
| Controller slice | `src/test` | `@WebMvcTest` + MockMvc + `@MockBean` | HTTP contract only | ~1–2 s |
| Integration | `src/integrationTest` | Testcontainers MySQL 8.1 | DAO ↔ real DB | ~5–15 s |
| Contract | `src/contractTest` | Pact 4.6 | Consumer/provider | varies |

```bash
./gradlew clean test        # fast gate — no Docker
./gradlew integrationTest   # authoritative — requires Docker
./gradlew check             # everything
./gradlew clean test jacocoTestReport
```

---

## Continuous Integration

| Job | What it does | Docker |
|---|---|---|
| `build-and-test` | Compiles **all** source sets, runs unit + slice tests, JaCoCo report | No |
| `integration-tests` | Testcontainers MySQL 8.1 suite (only if job 1 passes) | Yes |

The explicit compile step covers every source set including
`src/integrationTest` — a missing import there is invisible to `bootRun`,
which compiles only `src/main`. Reports upload on **every** run, including
failures.

---

## Bugs Caught by TDD

| Bug | Root cause | Caught by |
|---|---|---|
| Validation silently never fired | `==` used for String comparison — reference equality is never true for runtime strings | Domain unit test with a blank runtime string |
| Product inserted under the wrong key | `UUID.randomUUID()` instead of `product.getSkuId()` in the DAO insert | Testcontainers round-trip: insert succeeded, `findBySkuId` returned empty |
| Two DB round-trips per read | `queryForObject` called once for logging and again on return | Code review during the same integration test investigation |
| SQL parameter contract broken | Java variable name (`:obj_sku_id`) used as the SQL token instead of the schema column (`:skuId`) | Integration test failure on a real MySQL engine |
| `POST` returned 200 instead of 201 | Missing `@ResponseStatus(HttpStatus.CREATED)` | `@WebMvcTest` status assertion |
| `400` instead of `201` on valid input | Jackson derived the JSON key from `getName()` → `"name"`, constructor expected `productName` | MockMvc request-body output during a slice-test failure |
| `409` surfaced as `ServletException` | `GlobalExceptionHandler` did not exist, so domain exceptions were never mapped | `@WebMvcTest` expecting `isConflict()` |

---

## Repository Layout

> ⚠️ **The repository root and the Gradle root are different directories.**
> Both contain a `gradlew` and a `settings.gradle.kts`. Run `git` from the
> repository root and `./gradlew` from `product-catalog-service/`.

```
.                                   ← repository root (.git lives here)
├── .github/workflows/ci.yml
├── .gitignore
├── README.md
└── product-catalog-service/        ← Gradle root
    ├── build.gradle.kts
    ├── Dockerfile
    ├── docker-compose.yml
    ├── gradlew
    └── src/
        ├── main/java/com/example/product_catalog_service/
        │   ├── controller/     ProductController, GlobalExceptionHandler
        │   ├── service/        ProductService
        │   ├── dao/            DaoProductInterface, DaoProductImplementationJdbc
        │   ├── domainobjectmodel/  Product
        │   └── exception/      DuplicateSkuException, ProductNotFoundException
        ├── main/resources/db/migration/  V1__create_product_table.sql
        ├── test/java/...              unit + @WebMvcTest slice
        ├── integrationTest/java/...   Testcontainers
        └── contractTest/java/...      Pact
```

---

## Branch Model

| Branch | Role |
|---|---|
| `main` | Production-ready. Always green CI. Tagged releases. |
| `develop` | Integration branch — feature branches merge here first. |
| `feature/layer/phaseN/<layer>` | One architectural layer per branch. |
| `fix/*` | Bug fixes; root cause documented in the commit body. |

Completed layers are tagged (`phase0-domain`, `phase1-persistence`,
`phase2-service`) and their branches retired.

---

## Roadmap

- [x] **Phase 0 — Domain:** `Product` with constructor-enforced invariants, equivalence-partition tests
- [x] **Phase 1 — Persistence:** JDBC DAO, Flyway V1, Testcontainers integration tests
- [x] **Phase 2 — Service:** `createProduct` / `getProduct`, domain exceptions, Mockito unit tests
- [x] **Phase 3 — Web:** `ProductController`, `GlobalExceptionHandler`, `@WebMvcTest` suite, CI pipeline
- [ ] **Phase 3b:** `PUT` / `DELETE` / `GET`-all, request/response DTOs, `ApiResponse<T>` envelope
- [ ] **Phase 4 — Observability:** Micrometer + Prometheus + Grafana, latency baseline captured before optimization
- [ ] **Phase 5 — Security:** Spring Security + JWT on write endpoints
- [ ] **Phase 6 — Caching:** Redis cache-aside on `GET /products/{skuId}`, measured against the Phase 4 baseline

---

## Local Development

**Prerequisites:** Java 21, MySQL 8.x, Docker (integration tests only)

```bash
mysql -u root -p -e "CREATE DATABASE product_catalog \
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Configure src/main/resources/application-prod.properties
#   spring.datasource.url=jdbc:mysql://localhost:3306/product_catalog
#   spring.datasource.username=root
#   spring.datasource.password=${DB_PASSWORD}
# Never commit real credentials — this file is gitignored.

cd product-catalog-service
./gradlew bootRun     # → http://localhost:8080
```

---

## API Reference

| Method | Path | Success | Errors |
|---|---|---|---|
| `POST` | `/api/v1/products` | `201 Created` | `400` invalid body · `409` duplicate SKU |
| `GET` | `/api/v1/products/{skuId}` | `200 OK` | `404` not found |
| `PUT` | `/api/v1/products/{skuId}` | `200 OK` | `400` · `404` *(planned)* |
| `DELETE` | `/api/v1/products/{skuId}` | `204 No Content` | `404` *(planned)* |

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"skuId":"ABC-122","productName":"widget","price":95.00}'
# → 201

curl http://localhost:8080/api/v1/products/ABC-122
# → 200
```

**API versioning:** `/api/v1/` is explicit in the URL — cacheable, testable,
proxy-friendly. When `v2` arrives, `v1` keeps serving.

