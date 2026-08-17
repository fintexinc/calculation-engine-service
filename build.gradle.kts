plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.lombok) apply false
    alias(libs.plugins.jib) apply false
    alias(libs.plugins.git.properties) apply false
}

val springBootVersion = libs.versions.spring.boot.get()
val slf4jApiDep = libs.slf4j.api.get()
val junitJupiterApiDep = libs.junit.jupiter.api.get()
val junitJupiterEngineDep = libs.junit.jupiter.engine.get()
val mockitoCoreDep = libs.mockito.core.get()
val assertjCoreDep = libs.assertj.core.get()
val mockitoCoreCoord = mockitoCoreDep.toString()

// The tag a test run is limited to, e.g. `./gradlew test -Ptag=e2e`. Absent, everything runs — the same
// default the Maven build had, where `includeTags` was fed an unset `-Dtag` property.
val includedTag: String? = findProperty("tag") as String?

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

    group = "com.fintex"
    version = "1.0.0-tangerine-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "fintexincorporated"
            url = uri("https://pkgs.dev.azure.com/fintexincorporated/_packaging/fintexincorporated/maven/v1")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
    }

    // Mockito's inline mock maker has to be loaded as an agent: JDK 21 warns on self-attachment today and
    // will refuse it outright, so the jar is resolved as its own configuration and passed to every test JVM
    // rather than relying on dynamic loading. This is the Maven build's surefire argLine, minus the AspectJ
    // weaver it also loaded — nothing in these sources is an @Aspect, so that agent was dead weight.
    val mockitoAgent = configurations.create("mockitoAgent") {
        isTransitive = false
    }

    dependencies {
        "implementation"(slf4jApiDep)
        "testImplementation"(junitJupiterApiDep)
        "testImplementation"(mockitoCoreDep)
        "testImplementation"(assertjCoreDep)
        "testRuntimeOnly"(junitJupiterEngineDep)
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "mockitoAgent"(mockitoCoreCoord)
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            importOrderFile(rootProject.file("eclipse.importorder"))
            removeUnusedImports()
            eclipse().configFile(rootProject.file("eclipse-java-formatter.xml"))
        }
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includedTag?.let { includeTags(it) }
        }
        // Extensions are registered through ServiceLoader rather than @ExtendWith in some tests.
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        jvmArgumentProviders.add(CommandLineArgumentProvider {
            listOf("-javaagent:${mockitoAgent.singleFile.absolutePath}")
        })
        maxParallelForks = 8
    }
}
