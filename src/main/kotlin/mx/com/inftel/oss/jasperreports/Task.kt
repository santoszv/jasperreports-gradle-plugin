//    Jasper Reports Gradle Plugin
//    Copyright 2019 Santos Zatarain Vera
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

package mx.com.inftel.oss.jasperreports

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.full.primaryConstructor

const val JASPER_REPORTS_TASK_GROUP = "jasperreports"
const val JASPER_REPORTS_TASK_PREFIX = "jasperreports"

fun Project.setupJasperReportsTask(configurationName: String, extensionName: String, taskGroup: String, taskPrefix: String) {
    val command = "compileReports"
    val task = tasks.create(
            if (taskPrefix.isBlank()) command.decapitalize() else "$taskPrefix${command.capitalize()}",
            JasperReportsTask::class.java
    )
    task.configurationName = configurationName
    task.description = "Compile reports."
    task.extensionName = extensionName
    task.group = taskGroup
}


open class JasperReportsTask : DefaultTask() {

    @get:Internal
    lateinit var configurationName: String
    @get:Internal
    lateinit var extensionName: String

    @get:Internal
    val configuration: Configuration by lazy {
        project.configurations.getByName(configurationName)
    }
    @get:Internal
    val extension: JasperReportsExtension by lazy {
        project.extensions.getByName(extensionName) as JasperReportsExtension
    }

    @TaskAction
    fun run() {
        val urls = mutableListOf<URL>()
        val convention = project.convention
        val sourceSets = convention.findByType(SourceSetContainer::class.java)
                ?: convention.findPlugin(SourceSetContainer::class.java)
                ?: convention.getByType(SourceSetContainer::class.java)
                ?: throw UnsupportedOperationException()
        sourceSets.getByName(extension.sourceSet).output.files.forEach {
            val url = it.toURI().toURL()
            urls.add(url)
        }
        configuration.files.forEach {
            val url = it.toURI().toURL()
            urls.add(url)
        }
        val originalClassLoader = Thread.currentThread().contextClassLoader
        URLClassLoader(urls.toTypedArray(), originalClassLoader).use {
            Thread.currentThread().contextClassLoader = it
            try {
                val kClass = it.loadClass("mx.com.inftel.oss.jasperreports.helper.JasperReportsHelper").kotlin
                val kInstance = kClass.primaryConstructor!!.call(this) as Runnable
                kInstance.run()
            } finally {
                Thread.currentThread().contextClassLoader = originalClassLoader
            }
        }
    }
}