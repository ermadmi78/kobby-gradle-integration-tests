import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Configuration
import org.springframework.boot.gradle.plugin.SpringBootPlugin

description = "Cinema Server Example"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    `java-library`
    id("org.springframework.boot")
    id("org.flywaydb.flyway")
}

val flywayMigrationDir = file("${projectDir}/src/main/resources/db/migration")
val jooqOutputDir = file("${project.buildDir}/generated/sources/jooq/main/java")
val jooqSchemaDir = file("${project.buildDir}/jooq")
val jooqSchema = "cinema"
val jooqUrl = "jdbc:h2:${jooqSchemaDir.path}/${jooqSchema}"
val jooqUser = "sa"
val jooqPassword = ""

val kotlinJvmVersion: String by project
val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotestVersion: String by project
val flywaydbVersion: String by project
val h2Version: String by project
val jooqVersion: String by project
val jacksonVersion: String by project
val commonsLang3Version: String by project
val graphqlJavaKickstartVersion: String by project
val graphqlJavaExtendedScalarsVersion: String by project
val ktorVersion: String by project

flyway {
    url = jooqUrl
    user = jooqUser
    password = jooqPassword
    schemas = arrayOf(jooqSchema)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = kotlinJvmVersion
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        }
    }

    test {
        testLogging.showStandardStreams = true
        testLogging.exceptionFormat = FULL
        useJUnitPlatform()
    }

    flywayMigrate {
        inputs.dir(flywayMigrationDir)
        outputs.dir(jooqOutputDir)
        doFirst {
            project.delete(jooqSchemaDir)
        }
    }

    register("jooq") {
        dependsOn("flywayMigrate")

        inputs.dir(flywayMigrationDir)
        outputs.dir(jooqOutputDir)

        doFirst {
            project.delete(jooqOutputDir)
            GenerationTool.generate(
                Configuration()
                    .withJdbc(
                        Jdbc()
                            .withDriver("org.h2.Driver")
                            .withUrl(jooqUrl)
                            .withUser(jooqUser)
                            .withPassword(jooqPassword)
                    )
                    .withGenerator(
                        Generator()
                            .withDatabase(
                                Database()
                                    .withName("org.jooq.meta.h2.H2Database")
                                    .withInputSchema(jooqSchema)
                                    .withExcludes("flyway_schema_history")
                            )
                            .withGenerate(
                                Generate()
                                    .withPojos(true)
                                    .withDaos(true)
                            )
                            .withTarget(
                                org.jooq.meta.jaxb.Target()
                                    .withPackageName("io.github.ermadmi78.kobby.cinema.server.jooq")
                                    .withDirectory(jooqOutputDir.path)
                            )
                    )
            )
        }
    }

    named("compileKotlin") {
        dependsOn("jooq")
    }

    named("compileJava") {
        dependsOn("jooq")
    }
}
sourceSets {
    main {
        java {
            srcDir(jooqOutputDir)
        }
    }
}

buildscript {
    val h2Version: String by project
    val jooqVersion: String by project
    dependencies {
        classpath("com.h2database:h2:$h2Version")
        classpath("org.jooq:jooq-codegen:$jooqVersion")
    }
}
dependencies {
    implementation(project(":cinema-api"))

    implementation(
        "com.graphql-java-kickstart:" +
                "graphql-kickstart-spring-boot-starter-webflux:$graphqlJavaKickstartVersion"
    )
    implementation(
        "com.graphql-java-kickstart:" +
                "graphql-kickstart-spring-boot-starter-tools:$graphqlJavaKickstartVersion"
    )
    implementation(
        "com.graphql-java-kickstart:" +
                "graphiql-spring-boot-starter:$graphqlJavaKickstartVersion"
    )
    implementation(
        "com.graphql-java-kickstart:" +
                "playground-spring-boot-starter:$graphqlJavaKickstartVersion"
    )
    implementation(
        "com.graphql-java:" +
                "graphql-java-extended-scalars:$graphqlJavaExtendedScalarsVersion"
    ) {
        exclude("com.graphql-java", "graphql-java")
    }

    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-jooq") {
        exclude("org.jooq", "jooq")
    }
    implementation("org.jooq:jooq:$jooqVersion")

    implementation("org.flywaydb:flyway-core:$flywaydbVersion")
    implementation("com.h2database:h2:$h2Version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")

    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    testImplementation(kotlin("test", kotlinVersion))
    testImplementation(kotlin("test-junit5", kotlinVersion))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxCoroutinesVersion")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")

    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.ktor:ktor-client-websockets:$ktorVersion")
}