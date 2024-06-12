import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.8.0"
}

group = "io.eagle"
version = "1.0.32"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.guava:guava:31.0-jre")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-core:1.4.11")
    implementation("org.mongodb:bson:4.10.2")
    implementation("com.github.mfornos:humanize-slim:1.2.2")
    implementation("com.github.mfornos:humanize-joda:1.2.2")

    val jacksonVersion = "2.15.2"
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

publishing {
    repositories {
        maven {
            url = uri("s3://io-eagle-packages/maven")
            credentials(AwsCredentials::class) {
                accessKey = System.getenv("AWS_ACCESS_KEY_ID")
                secretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("src/main/java")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
