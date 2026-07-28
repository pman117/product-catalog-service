import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("org.springframework.boot") version "3.3.12"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val testcontainersVersion = "1.20.3"
val restAssuredVersion    = "5.5.0"
val pactVersion           = "4.6.15"

// ─── DEPENDENCY MANAGEMENT ───────────────────────────────────────────────────
// Pins Testcontainers BOM so all modules use the same version.
// Without this, individual testcontainers:* versions can diverge and conflict.
dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

// ─── DEPENDENCIES ────────────────────────────────────────────────────────────
dependencies {

    // ── Production ──────────────────────────────────────────────────────────

    // spring-boot-starter-web:
    //   Brings in: spring-webmvc, spring-web, Jackson, embedded Tomcat,
    //   AND transitively: hibernate-validator → jakarta.validation-api
    //   This is what your import jakarta.validation.Valid; needs at compile time.
    implementation("org.springframework.boot:spring-boot-starter-web")

    // FIX #1: spring-boot-starter-validation — THE MISSING DEPENDENCY
    // Best practice in Spring Boot 3.x: always declare this EXPLICITLY.
    // Reason: transitive inclusion via spring-boot-starter-web can be
    // disrupted by dependency resolution conflicts or classifier changes.
    // This starter directly declares:
    //   hibernate-validator → jakarta.validation:jakarta.validation-api
    // Without this, `import jakarta.validation.Valid` fails to compile.
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JDBC template — NamedParameterJdbcTemplate
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // Flyway — schema version control
    // FIX #2: flyway-mysql is required for Spring Boot 3.x / Flyway 10.x with MySQL.
    // Without flyway-mysql, Flyway 10+ throws:
    //   "No database found to handle jdbc:mysql://..."
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Database drivers
    runtimeOnly("com.h2database:h2")           // unit/slice tests (H2 in-memory)
    runtimeOnly("com.mysql:mysql-connector-j") // production + Testcontainers

    // Micrometer / Actuator — already wired metrics infrastructure
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Lombok — @Slf4j, @RequiredArgsConstructor, @Builder
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // FIX #3: Lombok also needed in test source sets
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // ── Test — unit + @WebMvcTest slice ─────────────────────────────────────
    // spring-boot-starter-test includes:
    //   JUnit 5 (junit-jupiter), Mockito, MockMvc, AssertJ, Hamcrest, Jackson
    //   @WebMvcTest requires this — it provides MockMvc + auto-configuration
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage") // exclude JUnit 4 runner
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ── Test — Testcontainers (MySQL integration tests) ──────────────────────
    // BOM above pins both to testcontainersVersion — no version needed here
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

    // ── Test — WireMock (HTTP stub server for service-to-service tests) ──────
    testImplementation("org.wiremock:wiremock:3.13.1")

    // ── Test — REST Assured (e2e HTTP test client) ───────────────────────────
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")

    // ── Test — Pact (consumer-driven contract tests) ──────────────────────────
    testImplementation("au.com.dius.pact.consumer:junit5:$pactVersion")
    testImplementation("au.com.dius.pact.provider:junit5spring:$pactVersion")
}

// ─── TEST TASK CONFIGURATION ─────────────────────────────────────────────────
tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading") // suppresses JVM agent warning on Java 21
    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED,
            TestLogEvent.PASSED
        )
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = false
    }
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

// Unit tests + @WebMvcTest slices — no Docker required, fast
tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// ─── JACOCO ───────────────────────────────────────────────────────────────────
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

// ─── ADDITIONAL TEST SOURCE SETS ─────────────────────────────────────────────
// FIX #4: Source sets must include main output + testRuntimeClasspath
// so they can see both production code AND all test dependencies (JUnit, Mockito, etc.)
// Without the classpath wiring below, integrationTest source set cannot
// find @SpringBootTest, @Testcontainers, etc. at compile time.
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        // Wire: sees main production code + all test dependencies
        compileClasspath += sourceSets.main.get().output +
                configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
    create("contractTest") {
        java.srcDir("src/contractTest/java")
        resources.srcDir("src/contractTest/resources")
        compileClasspath += sourceSets.main.get().output +
                configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
    create("e2eTest") {
        java.srcDir("src/e2eTest/java")
        resources.srcDir("src/e2eTest/resources")
        compileClasspath += sourceSets.main.get().output +
                configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}

// ─── INTEGRATION TEST TASK ────────────────────────────────────────────────────
val integrationTest by tasks.registering(Test::class) {
    description = "Runs Testcontainers MySQL integration tests (requires Docker)"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
    // FIX #5: integrationTest resource duplication workaround
    // (processIntegrationTestResources copies from both src/test/resources
    //  and src/integrationTest/resources — EXCLUDE prevents build failure)
}

// FIX #5 cont: resource duplication workaround for integrationTest source set
tasks.named<ProcessResources>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// ─── CONTRACT TEST TASK ───────────────────────────────────────────────────────
val contractTest by tasks.registering(Test::class) {
    description = "Runs Pact consumer/provider contract tests"
    group = "verification"
    testClassesDirs = sourceSets["contractTest"].output.classesDirs
    classpath = sourceSets["contractTest"].runtimeClasspath
    shouldRunAfter(integrationTest)
    useJUnitPlatform()
}

// ─── E2E TEST TASK ────────────────────────────────────────────────────────────
val e2eTest by tasks.registering(Test::class) {
    description = "Runs REST Assured end-to-end tests"
    group = "verification"
    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath
    shouldRunAfter(contractTest)
    useJUnitPlatform()
}

// ─── CHECK TASK ───────────────────────────────────────────────────────────────
// ./gradlew check runs: unit tests + integrationTest + contractTest
tasks.named("check") {
    dependsOn(integrationTest, contractTest)
}