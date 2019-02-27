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

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.*
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File
import java.lang.UnsupportedOperationException
import java.net.URLClassLoader
import kotlin.reflect.full.primaryConstructor

const val JASPER_REPORTS_TASK_GROUP = "jasper reports"
const val JASPER_REPORTS_TASK_NAME = "compileJasperReports"

fun Project.setupJasperReportsTask(taskGroup: String, taskName: String): JasperReportsTask {
    val task = tasks.create(taskName, JasperReportsTask::class.java)
    task.group = taskGroup
    task.description = "Compile Jasper Reports"
    return task
}

open class JasperReportsTask : DefaultTask() {

    @Classpath
    @InputFiles
    lateinit var classpath: MutableSet<File>
    @InputDirectory
    lateinit var inputDirectory: File
    @OutputDirectory
    lateinit var outputDirectory: File

    @Suppress("unused")
    @TaskAction
    fun run(inputs: IncrementalTaskInputs) {

        var compileAllReports = false
        if (!inputs.isIncremental) {
            compileAllReports = true
            outputDirectory.deleteRecursively()
        }

        val outOfDateReports = mutableSetOf<File>()
        inputs.outOfDate { detail ->
            when {
                detail.file.name.endsWith(".jar", true) -> compileAllReports = compileAllReports || true
                detail.file.name.endsWith(".class", true) -> compileAllReports = compileAllReports || true
                else -> outOfDateReports.add(detail.file)
            }
        }

        val removedReports = mutableSetOf<File>()
        inputs.removed { detail ->
            when {
                detail.file.name.endsWith(".jar", true) -> compileAllReports = compileAllReports || true
                detail.file.name.endsWith(".class", true) -> compileAllReports = compileAllReports || true
                else -> removedReports.add(detail.file)
            }
        }

        removedReports.forEach { report ->
            if (report.startsWith(inputDirectory)) {
                if ("jrxml".equals(report.extension, true)) {
                    val relative = report.toRelativeString(inputDirectory)
                    val tmp = File(outputDirectory, relative)
                    val target = File(tmp.parentFile, "${tmp.nameWithoutExtension}.jasper")
                    if (target.exists()) {
                        target.delete()
                    }
                    //println("Removed $relative")
                }
            }
        }

        if (compileAllReports) {
            inputDirectory.walk().forEach { file ->
                outOfDateReports.add(file)
            }
        }

        val originalClassLoader = Thread.currentThread().contextClassLoader
        URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), originalClassLoader).use { loader ->
            Thread.currentThread().contextClassLoader = loader
            try {
                val kClass = loader.loadClass("mx.com.inftel.oss.jasperreports.helper.JasperReportsHelper").kotlin
                outOfDateReports.forEach { report ->
                    if (report.startsWith(inputDirectory)) {
                        if ("jrxml".equals(report.extension, true)) {
                            val relative = report.toRelativeString(inputDirectory)
                            val tmp = File(outputDirectory, relative)
                            val target = File(tmp.parentFile, "${tmp.nameWithoutExtension}.jasper")
                            val kInstance = kClass.primaryConstructor!!.call(report, target) as Runnable
                            kInstance.run()
                            //println("Compiled $relative")
                        }
                    }
                }
            } catch (e: NoClassDefFoundError) {
                throw UnsupportedOperationException("Some required runtime class was not found", e)
            } catch (e: ClassNotFoundException) {
                throw UnsupportedOperationException("Some required runtime class was not found", e)
            } finally {
                Thread.currentThread().contextClassLoader = originalClassLoader
            }
        }
    }
}