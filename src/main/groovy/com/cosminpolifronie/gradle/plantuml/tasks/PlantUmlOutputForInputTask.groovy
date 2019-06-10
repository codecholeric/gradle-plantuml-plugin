package com.cosminpolifronie.gradle.plantuml.tasks

import com.cosminpolifronie.gradle.plantuml.PlantUmlException
import com.cosminpolifronie.gradle.plantuml.PlantUmlReceivedRender
import com.cosminpolifronie.gradle.plantuml.PlantUmlUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class PlantUmlOutputForInputTask extends DefaultTask {
    private Map<String, PlantUmlReceivedRender> inputReceivedRenderMap = [:]
    private String path

    @Option(option = 'path', description = 'The input file path for which the output has to be found')
    void setPath(String path) {
        this.path = path
    }

    void addReceivedRender(PlantUmlReceivedRender receivedRender) {
        inputReceivedRenderMap << [(receivedRender.input): receivedRender]
    }

    @TaskAction
    void execute() {
        if (path != null) {
            path = path.replace('\'', '')
            path = path.replace('\"', '')
            File outputFile = PlantUmlUtils.tryGetOutputFileForNotExistingInput(inputReceivedRenderMap, project, project.file(path))
            if (outputFile != null) {
                println(project.relativePath(outputFile).replace('\\', '/'))
            }
        } else {
            throw new PlantUmlException('This tasks has to be run with the --path option set. Usage: ./gradlew :plantUmlOutputForInput --path=\"your_path_here\"')
        }
    }
}
