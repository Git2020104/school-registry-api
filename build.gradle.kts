plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

group = "com.maranatha.skools"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.2.1")
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

dependencies {
    // Ktor Server
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.statusPages)

    // Authentication & JWT (catalog accessors or direct strings)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation("at.favre.lib:bcrypt:0.10.2")

    // JetBrains Exposed ORM (All matching 0.53.0)
    val exposedVersion = "0.53.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")

    // Database Drivers & Logging
    implementation(libs.h2database.h2)
    implementation(libs.postgresql)
    implementation(libs.logback.classic)

    // Database & Connection Pool
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Flyway Migration
    implementation("org.flywaydb:flyway-core:10.10.0")

    // OpenHTMLtoPDF for converting XHTML/CSS to PDF
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
    //implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.serialization.kotlinx.json)
}