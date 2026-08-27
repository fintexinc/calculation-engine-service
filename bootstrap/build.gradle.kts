plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.jib)
    alias(libs.plugins.git.properties)
}

description = "Spring Boot entry point and configuration"

dependencies {
    implementation(project(":application"))
    implementation(project(":observability-adapter"))
    implementation(project(":web-client-adapter"))
    implementation(project(":cache-adapter"))
    implementation(project(":rest-adapter"))

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.tracing.bridge.otel)

    testImplementation(testFixtures(project(":api")))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.resilience4j.spring.boot3)
    testImplementation(libs.okhttp.mockwebserver)
}

// build-info.properties and git.properties, as the Maven build produced them: nothing reads a placeholder
// out of either today, but PropertySourcesPlaceholderConfiguration loads git.properties off the classpath,
// so the release branch keeps the same runtime surface as main rather than a subtly emptier one.
springBoot {
    buildInfo()
}

/*
 * Every test class in this module boots a full Spring context and forkEvery = 1 gives each one its own JVM,
 * so the root project's eight parallel forks would mean eight applications starting at once. On a two-core
 * CI agent that starves the JVMs badly enough that HTTP calls inside a running test exceed their client
 * timeout. Raise it on a bigger machine with -PbootstrapForks=8.
 */
tasks.withType<Test> {
    maxParallelForks = (findProperty("bootstrapForks") as String?)?.toInt() ?: 2
    forkEvery = 1
}

jib {
    from {
        image =
            "eclipse-temurin:21-jre-alpine@sha256:6ad8ed080d9be96b61438ec3ce99388e294af216ed57356000c06070e85c5d5d"
    }
    to {
        image = "portfolio-calculation-engine:${project.version}"
    }
    container {
        user = "1000:1000"
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8080")
        jvmFlags = listOf("-XX:MaxRAMPercentage=75", "-XX:InitialRAMPercentage=40")
        labels = mapOf(
            "org.opencontainers.image.source" to
                "https://dev.azure.com/fintexincorporated/portfolio-calculation-engine",
            "org.opencontainers.image.title" to "portfolio-calculation-engine"
        )
    }
}
