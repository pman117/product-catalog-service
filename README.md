develop branch: Integration branch; all features merge here first

A production-grade **product catalog microservice** built with strict
Test-Driven Development (TDD). Every layer (Domain → Persistence →
Service → Web) was implemented red→green→refactor with a dedicated
test type per layer.

---

## Tech Stack

| Layer | Technology | Decision |
|-------|-----------|----------|
| Language | Java 21 (LTS) | Current LTS — virtual threads, records, pattern matching |
| Framework | Spring Boot 3.3.12 | Jakarta EE 10, native image ready |
| Persistence | NamedParameterJdbcTemplate | Explicit SQL — DECIMAL(65,30) financial precision |
| Schema migrations | Flyway 10.x | Version-controlled, auditable migrations |
| Database (prod) | MySQL 8.x | Production target |
| Database (test) | Testcontainers MySQL 8.1 | Real engine in integration tests |
| Build | Gradle 8.14.2 (Kotlin DSL) | 4 test source sets |
| Unit tests | JUnit 5 + Mockito | @ExtendWith(MockitoExtension) |
| Controller tests | @WebMvcTest + MockMvc | HTTP contract slice — no DB required |
| Integration tests | Testcontainers | Real MySQL — authoritative |
| Validation | Jakarta Bean Validation | @Valid on @RequestBody |
| Exception handling | @RestControllerAdvice | GlobalExceptionHandler |

---

## Architecture

```
HTTP Client (Postman / REST Assured)
    │
    ▼
ProductController  (@RestController)
    │  @Valid @RequestBody Product
    │  @ResponseStatus(CREATED / NO_CONTENT)
    │  GlobalExceptionHandler (@RestControllerAdvice)
    ▼
ProductService  (@Service, @Transactional)
    │  Business rules: duplicate check, existence guard
    │  Exception translation: Optional.empty() → ProductNotFoundException
    ▼
DaoProductInterface  (interface)
    │
    ▼
DaoProductImplementationJdbc  (@Repository)
    │  NamedParameterJdbcTemplate — named SQL params
    │  static final RowMapper<Product> — thread-safe, one instance
    │  InitializingBean.afterPropertiesSet() — null guard
    ▼
MySQL 8.x  (DECIMAL(65,30) price column)
    │
    ▼
Flyway V1__create_product_table.sql
```

---

## Key Architectural Decisions

### Why NamedParameterJdbcTemplate over Spring Data JPA?
Product price is stored as `DECIMAL(65,30)` — 65 significant digits,
30 decimal places. IEEE 754 `double` is binary-fraction and introduces
rounding drift (9.99 stored as 9.9899999...). JPA/Hibernate can silently
downcast BigDecimal to double during type mapping. Explicit JDBC with
`getBigDecimal()` guarantees exact precision end-to-end.

### Why Testcontainers MySQL 8.1 over H2 for integration tests?
H2 MySQL compatibility mode is an approximation — different engine,
different strict mode enforcement, different DECIMAL handling. Testcontainers
runs real MySQL 8.1 (same version as production). The integration tests
are authoritative, not approximations.

### Why @Transactional at service layer, not DAO?
Transaction boundary = business operation boundary.
`createProduct` = `existsBySkuId` + `insert` — these must be atomic.
DAO is persistence concern. Controller is HTTP concern.
Neither owns transaction lifecycle.

### Why Optional<Product> on DAO but Product on Service?
DAO cannot know if "not found" is an error or normal — returns Optional.
Service converts `Optional.empty()` → `ProductNotFoundException` via
`.orElseThrow()` — one DB call, correct exception, no double-query.

---

## Test Strategy — 4 Tiers

| Tier | Source Set | Technology | Scope | Speed |
|------|-----------|-----------|-------|-------|
| Unit | src/test | JUnit 5 + Mockito | Domain + Service logic | ~50ms |
| Controller | src/test | @WebMvcTest + MockMvc + @MockBean | HTTP contract | ~1-2s |
| Integration | src/integrationTest | Testcontainers MySQL 8.1 | DAO + DB | ~5-15s |
| Contract | src/contractTest | Pact 4.6 | Consumer/provider | varies |

```bash
# Unit + controller tests (no Docker required)
./gradlew clean test

# Integration tests (requires Docker)
./gradlew integrationTest

# All checks
./gradlew check
```

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready, always green CI |
| `develop` | Integration — all features merge here first |
| `feature/layer/phase0/domain` | Domain model + validation ✅ |
| `feature/layer/phase1/persistence` | DAO + Testcontainers ✅ |
| `feature/layer/phase2/service` | Service + Mockito unit tests ✅ |
| `feature/layer/phase3/web` | Controller + @WebMvcTest (in progress) |
| `fix/*` | Bug fixes with root-cause commit messages |

---

## API Endpoints

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | /api/v1/products | 201 Created | Create product |
| GET | /api/v1/products/{skuId} | 200 OK / 404 | Get by skuId |
| PUT | /api/v1/products/{skuId} | 200 OK / 404 | Update product |
| DELETE | /api/v1/products/{skuId} | 204 No Content / 404 | Delete product |

---

## Local Development

**Prerequisites:** Java 21, MySQL 8.x running locally, Docker (for integration tests)

```bash
# Create MySQL schema
mysql -u root -p
CREATE DATABASE product_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Set properties in src/main/resources/application-prod.properties
spring.datasource.url=jdbc:mysql://localhost:3306/product_catalog
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# Run (Flyway auto-creates the product table on first start)
./gradlew bootRun

# App available at http://localhost:8080
```

## Postman Examples

```bash
# Create
POST http://localhost:8080/api/v1/products
Content-Type: application/json
{"skuId":"ABC-122","productName":"widget","price":95.00}

# Retrieve
GET http://localhost:8080/api/v1/products/ABC-122

