import java.io.File

plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "self.tekichan.s3mate"
version = project.findProperty("version")
    ?.toString()
    ?.takeIf { it.isNotBlank() && it != "unspecified" }
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
    modules = listOf(
        "javafx.controls",
        "javafx.fxml"
    )
}

application {
    mainClass.set("self.tekichan.s3mate.MainApp")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }
}

tasks.build {
    dependsOn("jar")
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Build fat JAR with all dependencies (classpath-based)"

    archiveBaseName.set("s3mate")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("all")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }

    // Include compiled classes
    from(sourceSets.main.get().output)

    // Include runtime dependencies
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}

/**
 * macOS app-image using jpackage
 * Output:
 *   build/jpackage/mac/S3Mate.app
 */
tasks.register<Exec>("jpackageMac") {
    dependsOn("installDist")

    val runtimeClasspath = configurations.runtimeClasspath.get()
        .filter { it.name.contains("javafx") }
        .joinToString(File.pathSeparator) { it.absolutePath }

    commandLine(
        "jpackage",
        "--type", "app-image",
        "--name", "S3Mate",
        "--input", "build/install/s3mate/lib",
        "--main-jar", "s3mate-${project.version}.jar",
        "--main-class", "self.tekichan.s3mate.MainApp",
        "--module-path", runtimeClasspath,
        "--add-modules", "javafx.controls,javafx.fxml",
        "--dest", "build/jpackage/mac",
        "--verbose"
    )
}

tasks.register<Exec>("jpackageWin") {
    dependsOn("installDist")

    val javafxModulePath = configurations.runtimeClasspath.get()
        .filter { it.name.contains("javafx") }
        .joinToString(File.pathSeparator) { it.absolutePath }

    commandLine(
        "jpackage",
        "--type", "exe",
        "--name", "S3Mate",
        "--input", "build/install/s3mate/lib",
        "--main-jar", "s3mate-${project.version}.jar",
        "--main-class", "self.tekichan.s3mate.MainApp",
        "--module-path", javafxModulePath,
        "--add-modules", "javafx.controls,javafx.fxml",
        "--dest", "build/jpackage/win",
        "--verbose"
    )
}
