import com.diffplug.spotless.LineEnding
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
version = libs.versions.keycloak.get()

dependencies {
    compileOnly(libs.keycloak.core)
    compileOnly(libs.keycloak.services)
    compileOnly(libs.keycloak.serverSpi)
    compileOnly(libs.keycloak.serverSpi.private)
    implementation(libs.google.apiClient)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
    }
    lineEndings = LineEnding.PLATFORM_NATIVE
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.test { useJUnitPlatform() }
