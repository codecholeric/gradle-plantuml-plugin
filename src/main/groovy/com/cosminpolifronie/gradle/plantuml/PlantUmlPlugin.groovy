package com.cosminpolifronie.gradle.plantuml

import com.cosminpolifronie.gradle.plantuml.tasks.PlantUmlIOTask
import com.cosminpolifronie.gradle.plantuml.tasks.PlantUmlOutputForInputTask
import com.cosminpolifronie.gradle.plantuml.tasks.PlantUmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlantUmlPlugin implements Plugin<Project> {
    void apply(Project project) {
        def extension = project.extensions.create('plantUml', PlantUmlPluginExtension)

        project.tasks.register('plantUml', PlantUmlTask) {
            description = 'Renders PlantUML files to images'
            group = 'PlantUML Plugin'

            PlantUmlUtils.prepareRenders(project, extension.receivedRenders).each { entry ->
                addPreparedRender(entry)
            }

            extension.receivedRenders.each { entry ->
                addReceivedRender(entry)
            }
        }

        project.tasks.register('plantUmlIO', PlantUmlIOTask) {
            description = 'Prints the recognized inputs with their corresponding outputs'
            group = 'PlantUML Plugin'

            PlantUmlUtils.prepareRenders(project, extension.receivedRenders).each { entry ->
                addPreparedRender(entry)
            }
        }

        project.tasks.register('plantUmlOutputForInput', PlantUmlOutputForInputTask) {
            description = 'Tries to display the output for the given input using the available entries in build.gradle'
            group = 'PlantUML Plugin'

            extension.receivedRenders.each { entry ->
                addReceivedRender(entry)
            }
        }
    }
}
