import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.4.21"
}

group = "io.eagle"
version = "2.0.0"

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

    val jacksonVersion = "3.1.8"
    api(platform("tools.jackson:jackson-bom:$jacksonVersion"))
    api("tools.jackson.core:jackson-databind")
    api("tools.jackson.module:jackson-module-kotlin")
    implementation("tools.jackson.datatype:jackson-datatype-joda")

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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
