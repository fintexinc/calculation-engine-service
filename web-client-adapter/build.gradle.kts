description = "Retrieves data FROM Security Master via REST API"

dependencies {
    api(project(":api"))
    api(libs.spring.boot.starter.webflux)
    api(libs.jackson.databind)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.okhttp.mockwebserver)
}
