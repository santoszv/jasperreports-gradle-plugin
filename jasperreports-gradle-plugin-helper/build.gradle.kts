plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    // General
    compileOnly(gradleApi())
    compile(kotlin("stdlib-jdk8"))

    // Plugin
    compileOnly(project(":"))

    // Jasper Reports
    compileOnly("net.sf.jasperreports:jasperreports:6.7.0")
}

publishing {
    publications {
        create<MavenPublication>("helper") {
            from(components["java"])
        }
    }
}