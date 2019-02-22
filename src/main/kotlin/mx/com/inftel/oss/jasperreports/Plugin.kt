/*
 Jasper Reports Gradle Plugin
 Copyright 2019 Santos Zatarain Vera

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package mx.com.inftel.oss.jasperreports

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import java.io.File

@Suppress("unused")
class JasperReportsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val configuration = target.setupJasperReportsConfiguration(JASPER_REPORTS_CONFIGURATION)
        val extension = target.setupJasperReportsExtension(JASPER_REPORTS_EXTENSION)
        val task = target.setupJasperReportsTask(JASPER_REPORTS_TASK_GROUP, JASPER_REPORTS_TASK_NAME)
        target.afterEvaluate { _ ->
            val compileClasspath = mutableSetOf<File>()
            target.dependencies.add(JASPER_REPORTS_CONFIGURATION, "${JasperReportsVersion.group}:${JasperReportsVersion.name}-helper:${JasperReportsVersion.version}")
            if (extension.libraryVersion.isNotBlank()) {
                target.dependencies.add(JASPER_REPORTS_CONFIGURATION, "net.sf.jasperreports:jasperreports:${extension.libraryVersion}")
            }
            compileClasspath.addAll(configuration.files)
            if (extension.sourceSet.isNotBlank()) {
                val sourceSets = target.convention.findByType(SourceSetContainer::class.java)
                        ?: target.convention.findPlugin(SourceSetContainer::class.java)
                        ?: target.convention.getByType(SourceSetContainer::class.java)
                        ?: throw UnsupportedOperationException("Source set container not found")
                val sourceSet = sourceSets.findByName(extension.sourceSet) ?: throw UnsupportedOperationException("Source set ${extension.sourceSet} not found")
                compileClasspath.addAll(sourceSet.output.files)
                target.tasks.findByName("compileJava")?.let { compile ->
                    task.dependsOn(compile)
                }
                target.tasks.findByName("compileKotlin")?.let { compile ->
                    task.dependsOn(compile)
                }
            }
            val jar = target.tasks.findByName("jar") as? Jar
            if (jar != null) {
                jar.dependsOn(task)
                jar.from(extension.outputReportsDir).include("**/*.jasper")
            }
            val war = target.tasks.findByName("war") as? War
            if (war != null) {
                war.dependsOn(task)
                war.from(extension.outputReportsDir).include("**/*.jasper").into("WEB-INF/classes")
            }
            task.classpath = compileClasspath
            task.inputDirectory = target.file(extension.inputReportsDir)
            task.outputDirectory = target.file(extension.outputReportsDir)
        }
    }
}