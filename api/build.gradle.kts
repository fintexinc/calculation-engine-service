description = "Port interfaces (input and output)"

dependencies {
    api(project(":domain"))
    api(libs.jackson.annotations)
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.guava)
    api(libs.commons.lang3)

    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.commons.math3)
}
