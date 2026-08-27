description = "Retrieves data FROM the Market Investment Catalogue via REST API"

dependencies {
    api(project(":api"))
    api(libs.spring.boot.starter.webflux)
    api(libs.jackson.databind)
    api(libs.resilience4j.circuitbreaker)
    api(libs.resilience4j.retry)
    api(libs.resilience4j.timelimiter)
    api(libs.resilience4j.reactor)
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.micrometer)
    implementation(libs.micrometer.context.propagation)

    testImplementation(testFixtures(project(":api")))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.okhttp.mockwebserver)
}
