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

package mx.com.inftel.oss.jasperreports.helper

import mx.com.inftel.oss.jasperreports.JasperReportsExtension
import mx.com.inftel.oss.jasperreports.JasperReportsTask
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JasperCompileManager
import java.io.File
import java.nio.file.Files

const val JASPER_REPORTS_ERROR = "Jasper Reports Error"

class JasperReportsHelper(val task: JasperReportsTask) : Runnable {

    val extension: JasperReportsExtension by lazy {
        task.project.extensions.getByName(task.extensionName) as JasperReportsExtension
    }

    override fun run() {
        try {
            val inputDir = absolutePathFile(extension.inputReportsDir) ?: throw IllegalArgumentException("$JASPER_REPORTS_ERROR: Input path is required.")
            val outputDir = absolutePathFile(extension.outputReportsDir) ?: throw IllegalArgumentException("$JASPER_REPORTS_ERROR: Output path is required.")
            val reports = mutableListOf<String>()
            Files.walk(inputDir.toPath()).use { s ->
                s.forEach { p ->
                    val f = p.toFile().absoluteFile
                    if (f.startsWith(inputDir) && f.name.endsWith(".jrxml", true)) {
                        reports.add(f.relativeTo(inputDir).path)
                    }
                }
            }
            println("Compiling ${reports.size} reports")
            reports.forEach { r ->
                println(r)
                val input = File(inputDir, r)
                val output = File(outputDir, "${r.substring(0, r.length - 6)}.jasper")
                output.parentFile.mkdirs()
                JasperCompileManager.compileReportToFile(input.absolutePath, output.absolutePath)
            }
            println("Compiled ${reports.size} reports")
        } catch (e: JRException) {
            throw Exception("$JASPER_REPORTS_ERROR: ${e.message}")
        }
    }

    private fun absolutePathFile(path: String?): File? {
        if (path == null) return null
        val file = File(path)
        return if (file.isAbsolute) {
            file.absoluteFile
        } else {
            task.project.file(path).absoluteFile
        }
    }
}