import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    compileOnly(gradleApi())
    compile(kotlin("stdlib-jdk8"))
    compileOnly("net.sf.jasperreports:jasperreports:6.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("helper") {
            from(components["java"])
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