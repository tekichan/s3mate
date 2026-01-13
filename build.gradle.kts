plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "self.tekichan.s3mate"
version = project.findProperty("version")
    ?.toString()
    ?.takeIf { it.isNotBlank() && "unspecified" != it }
    ?: "SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.25.60"))
    implementation("software.amazon.awssdk:s3")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("self.tekichan.s3mate.MainApp")
}

tasks.test {
    useJUnitPlatform()
}

val jarBaseName = "s3mate"
val jarVersion = project.version

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("s3mate")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")   // no "-all"

        destinationDirectory.set(layout.buildDirectory.dir("generated"))
        mergeServiceFiles()

        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
    }

    build {
        dependsOn(shadowJar)
    }
}