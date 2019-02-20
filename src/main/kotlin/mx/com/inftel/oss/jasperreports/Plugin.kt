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

import org.gradle.api.Plugin
import org.gradle.api.Project

class JasperReportsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.setupJasperReportsConfiguration(JASPER_REPORTS_CONFIGURATION)
        target.setupJasperReportsExtension(JASPER_REPORTS_EXTENSION)
        target.setupJasperReportsTask(
                JASPER_REPORTS_CONFIGURATION,
                JASPER_REPORTS_EXTENSION,
                JASPER_REPORTS_TASK_GROUP,
                JASPER_REPORTS_TASK_PREFIX
        )
        target.dependencies.add(JASPER_REPORTS_CONFIGURATION, "${JasperReportsVersion.group}:${JasperReportsVersion.name}-helper:${JasperReportsVersion.version}")
    }
}