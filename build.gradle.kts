import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {

    group = "mx.com.inftel.oss"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
        mavenCentral()
        maven("https://jaspersoft.jfrog.io/jaspersoft/third-party-ce-artifacts")
    }

    afterEvaluate {

        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
        }

        publishing {
            repositories {
                val ossrhEnabled = properties["ossrhEnabled"]?.toString()?.toBoolean() ?: false
                if (ossrhEnabled) {
                    maven {
                        val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                        val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                        url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                        credentials {
                            username = properties["ossrhUsername"]?.toString()
                            password = properties["ossrhPassword"]?.toString()
                        }
                        println("Repository: $url")
                    }
                } else {
                    maven {
                        url = uri("../jasperreports-gradle-plugin-sample/repo")
                        println("Repository: $url")
                    }
                }
            }
        }

    }
}

plugins {
    kotlin("jvm") version "1.3.21"
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    // General
    compileOnly(gradleApi())
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
}

val generatedSourcesDir = file("$buildDir/generated/src")

sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir(generatedSourcesDir)
}

val generateSources by tasks.creating(DefaultTask::class) {
    outputs.dir(generatedSourcesDir)
    doFirst {
        generatedSourcesDir.exists() || generatedSourcesDir.mkdirs()
        val file = File(generatedSourcesDir, "Version.kt")
        file.writeText("""
            package mx.com.inftel.oss.jasperreports
            object JasperReportsVersion {
                val group = "${project.group}"
                val name = "${project.name}"
                val version = "${project.version}"
            }
        """.trimIndent())
    }
}

val compileKotlin by tasks.getting(KotlinCompile::class)
compileKotlin.dependsOn(generateSources)

gradlePlugin {
    plugins {
        create("jasperreports") {
            id = "mx.com.inftel.jasperreports"
            implementationClass = "mx.com.inftel.oss.jasperreports.JasperReportsPlugin"
        }
    }
}
