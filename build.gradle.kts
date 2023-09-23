import com.diffplug.spotless.LineEnding

plugins {
    java
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    google()
}

group = "dev.avatsav"

version = "1.0"

dependencies {
    compileOnly(libs.keycloak.core)
    compileOnly(libs.keycloak.services)
    compileOnly(libs.keycloak.serverSpi)
    compileOnly(libs.keycloak.serverSpi.private)

    implementation(libs.google.apiClient)

    compileOnly(libs.google.autoService)
    annotationProcessor(libs.google.autoService)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
    }
    lineEndings = LineEnding.PLATFORM_NATIVE
}

tasks.test { useJUnitPlatform() }
