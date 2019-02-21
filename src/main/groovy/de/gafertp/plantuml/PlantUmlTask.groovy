package de.gafertp.plantuml

import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class PlantUmlTask extends DefaultTask {
    Map<File, PlantUmlPreparedRender> inputPreparedRenderMap = [:]

    @InputFiles
    List<File> inputFiles = []

    @OutputFiles
    List<File> outputFiles = []

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        if (!inputs.incremental)
            project.delete(outputFiles)

        inputs.outOfDate { change ->
            if (inputPreparedRenderMap.containsKey(change.file)) {
                def preparedRender = inputPreparedRenderMap[change.file]
                preparedRender.output.withOutputStream { out ->
                    new SourceStringReader(preparedRender.input.text).outputImage(out, new FileFormatOption(preparedRender.format))
                }
            }
        }

        inputs.removed { change ->
            if (inputPreparedRenderMap.containsKey(change.file)) {
                def preparedRender = inputPreparedRenderMap[change.file]
                if (preparedRender.output.exists()) {
                    preparedRender.output.delete()
                }
            }
        }
    }
}
