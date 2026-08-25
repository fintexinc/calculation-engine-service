description = "Pure domain models and calculation logic with no framework dependencies"

dependencies {
    api(libs.catalogue.investment.commons)
    api(libs.jackson.annotations)
    api(libs.swagger.annotations.jakarta)
    api(libs.guava)

    testImplementation(libs.junit.jupiter.params)
}
