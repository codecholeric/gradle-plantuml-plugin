package de.gafertp.plantuml

import org.gradle.api.Plugin
import org.gradle.api.Project

class PlantUmlPlugin implements Plugin<Project> {
    void apply(Project project) {
        def extension = project.extensions.create('plantUml', PlantUmlPluginExtension)

        project.tasks.register('plantUml', PlantUmlTask) {
            prepareRenders(project, extension.receivedRenders).each { entry ->
                inputFiles << entry.input
                outputFiles << entry.output
                inputPreparedRenderMap << [(entry.input): entry]
            }
        }
    }

    private static List<PlantUmlPreparedRender> prepareRenders(Project project, List<PlantUmlReceivedRender> receivedRenders) {
        List<PlantUmlPreparedRender> preparedRenders = []

        receivedRenders.each { render ->
            def matchingFileNames = new FileNameFinder().getFileNames(project.file('.').absolutePath, render.input)
            def outputFile = project.file(render.output)

            def isDirectFileRendering = matchingFileNames.size() == 1 &&
                    !outputFile.directory &&
                    outputFile.name.endsWith(render.format.fileSuffix)

            if (isDirectFileRendering) {
                def inputFile = new File(matchingFileNames[0])
                assert outputFile.parentFile.exists() || outputFile.parentFile.mkdirs()
                preparedRenders << new PlantUmlPreparedRender(inputFile, outputFile, render.format)
            } else {
                assert outputFile.exists() || outputFile.mkdirs(): "Cannot create directory ${outputFile.absolutePath}"
                assert outputFile.isDirectory():
                        "Input ${matchingFileNames} matches multiple files, but output ${outputFile.absolutePath} is no directory"

                def outputFolder = outputFile

                matchingFileNames.each { inputFilePath ->
                    def inputFile = new File(inputFilePath)
                    def outputFileName = "${inputFile.name.take(inputFile.name.lastIndexOf('.'))}${render.format.fileSuffix}"
                    outputFile = new File(outputFolder, outputFileName)

                    assert outputFile.parentFile.exists() || outputFile.parentFile.mkdirs()
                    preparedRenders << new PlantUmlPreparedRender(inputFile, outputFile, render.format)
                }
            }
        }

        return preparedRenders
    }
}
