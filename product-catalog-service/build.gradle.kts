import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("org.springframework.boot") version "3.3.12"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    // Gatling plugin optional: add apply true if you plan perf tests via Gradle
    // id("com.github.lkishalmi.gatling") version "3.9.5" apply false
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
val mockkVersion = "1.13.9"
val wiremockVersion = "3.5.2"
val restAssuredVersion = "5.5.0"
val pactVersion = "4.6.15"

dependencies {
    // production
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2")             // fast dev DB
    runtimeOnly("com.mysql:mysql-connector-j")   // production connector

    // unit & slice testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
    testImplementation("io.mockk:mockk:$mockkVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // integration (Testcontainers)
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:mysql:$testcontainersVersion")

    // HTTP / stubbing / verification
    //testImplementation("com.github.tomakehurst:wiremock:$wiremockVersion")
    testImplementation("org.wiremock:wiremock:3.13.1") 
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")

    // contract testing (Pact) - if you prefer SCC, swap these
    testImplementation("au.com.dius.pact.consumer:junit5:$pactVersion")
    testImplementation("au.com.dius.pact.provider:junit5spring:$pactVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = false
    }
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

// --- Additional test source sets (Kotlin and Java friendly) ---
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        // If you prefer Kotlin, add: kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
    }
    create("contractTest") {
        java.srcDir("src/contractTest/java")
        resources.srcDir("src/contractTest/resources")
    }
    create("e2eTest") {
        java.srcDir("src/e2eTest/java")
        resources.srcDir("src/e2eTest/resources")
    }
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests using Testcontainers"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

val contractTest by tasks.registering(Test::class) {
    description = "Runs contract tests (Pact/SCC)"
    group = "verification"
    testClassesDirs = sourceSets["contractTest"].output.classesDirs
    classpath = sourceSets["contractTest"].runtimeClasspath
    shouldRunAfter(integrationTest)
    useJUnitPlatform()
}

val e2eTest by tasks.registering(Test::class) {
    description = "Runs end-to-end tests"
    group = "verification"
    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath
    shouldRunAfter(contractTest)
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn(integrationTest, contractTest)
}
																																																																																																																																																																																				
