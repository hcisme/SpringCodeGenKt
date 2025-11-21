plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    application
    id("com.gradleup.shadow") version "9.2.2"
}

group = "io.github.hcisme"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("$group.springcodegenkt.SpringCodeGenApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    implementation("ch.qos.logback:logback-core:1.5.21")
    implementation("com.charleskorn.kaml:kaml:0.102.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

val copyConfigToLibs = tasks.register<Copy>("copyConfigToLibs") {
    from("src/main/resources") {
        include("application.yml")
    }
    into(tasks.shadowJar.get().destinationDirectory)
}

tasks.shadowJar {
    finalizedBy(copyConfigToLibs)
}