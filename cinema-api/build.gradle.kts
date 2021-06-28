import io.github.ermadmi78.kobby.kobby

description = "Cinema API Example"

plugins {
    kotlin("jvm")
    `java-library`
    id("io.github.ermadmi78.kobby") version "1.0.0-beta.06"
}

kobby {
    kotlin {
        scalars = mapOf(
            "Date" to typeOf("java.time", "LocalDate"),
            "JSON" to typeMap.parameterize(typeString, typeAny.nullable())
        )
    }
}

val kotlinJvmVersion: String by project
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = kotlinJvmVersion
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}

val jacksonVersion: String by project
val ktorVersion: String by project
val graphqlJavaKickstartVersion: String by project
val reactivestreamsVersion: String by project
val kotlinVersion: String by project
dependencies {
    // Add this dependency to enable Jackson annotation generation in DTO classes by Kobby
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    // Add this dependency to enable default Ktor adapters generation
    compileOnly("io.ktor:ktor-client-cio:$ktorVersion")

    // Add this dependency to enable graphql-java-kickstart resolvers generation by Kobby
    compileOnly("com.graphql-java-kickstart:graphql-java-tools:$graphqlJavaKickstartVersion")

    // Add this dependency to enable subscription resolver publisher generation
    compileOnly("org.reactivestreams:reactive-streams:$reactivestreamsVersion")

    // Add this dependencies to remove warning "Runtime JAR files in the classpath should have the same version"
    compileOnly(kotlin("stdlib", kotlinVersion))
    compileOnly(kotlin("stdlib-jdk7", kotlinVersion))
    compileOnly(kotlin("reflect", kotlinVersion))
}