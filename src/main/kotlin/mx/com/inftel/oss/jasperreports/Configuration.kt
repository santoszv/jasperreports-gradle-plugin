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

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

const val JASPER_REPORTS_CONFIGURATION = "jasperreports"

fun Project.setupJasperReportsConfiguration(name: String) {
    configurations.maybeCreate(name)
}

fun DependencyHandler.jasperreports(dependencyNotation: Any): Dependency? =
        add(JASPER_REPORTS_CONFIGURATION, dependencyNotation)
