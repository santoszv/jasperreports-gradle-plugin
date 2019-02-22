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
}

plugins {
    kotlin("jvm") version "1.3.21"
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    compileOnly(gradleApi())
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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

publishing {
    repositories {
        val ossrhEnabled = properties["ossrhEnabled"]?.toString()?.toBoolean() ?: false
        if (ossrhEnabled) {
            val repoUrl =
                    if (version.toString().endsWith("SNAPSHOT"))
                        "https://oss.sonatype.org/content/repositories/snapshots/"
                    else
                        "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            maven {
                url = uri(repoUrl)
                credentials {
                    username = properties["ossrhUsername"]?.toString()
                    password = properties["ossrhPassword"]?.toString()
                }
            }
            println("maven $repoUrl")
        } else {
            mavenLocal()
            println("maven local")
        }
    }
}
