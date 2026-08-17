description = "Optional cache-proxy implementations (Caffeine / Redis) for api ports"

dependencies {
    api(project(":api"))
    api(libs.spring.boot.starter)
    api(libs.caffeine)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.junit.jupiter)
}
