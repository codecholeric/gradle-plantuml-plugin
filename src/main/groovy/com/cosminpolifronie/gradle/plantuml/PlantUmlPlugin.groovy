package com.cosminpolifronie.gradle.plantuml

import net.sourceforge.plantuml.FileFormat
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlantUmlPlugin implements Plugin<Project> {
    void apply(Project project) {
        def extension = project.extensions.create('plantUml', PlantUmlPluginExtension)

        project.tasks.register('plantUml', PlantUmlTask) {
            prepareRenders(project, extension.receivedRenders).each { entry ->
                addPreparedRender(entry)
            }

            extension.receivedRenders.each { entry ->
                addReceivedRender(entry)
            }
        }

        project.tasks.register('plantUmlIO') {
            prepareRenders(project, extension.receivedRenders).each { entry ->
                project.println("${project.relativePath(entry.input).replace('\\', '/')},${project.relativePath(entry.output).replace('\\', '/')}")
            }
        }

        project.tasks.register('plantUmlOutputForInput') {
            Map<String, PlantUmlReceivedRender> inputReceivedRenderMap = [:]

            extension.receivedRenders.each { entry ->
                inputReceivedRenderMap << [(entry.input): entry]
            }

            if (project.hasProperty('plantumloutputforinputpath')) {
                String input_path = project.property('plantumloutputforinputpath')
                File output_file = PlantUmlUtils.tryGetOutputFileForNotExistingInput(inputReceivedRenderMap, project, project.file(input_path))
                if (output_file != null) {
                    project.println(project.relativePath(output_file).replace('\\', '/'))
                }
            } else {
                project.println('This task has to be run with the \'plantumloutputforinputpath\' property set. Usage: ./gradlew :plantUmlOutputForInput -Pplantumloutputforinputpath=\"your_path_here\"')
            }
        }
    }

    private static List<PlantUmlPreparedRender> prepareRenders(Project project, List<PlantUmlReceivedRender> receivedRenders) {
        List<PlantUmlPreparedRender> preparedRenders = []

        receivedRenders.each { render ->
            def matchingFileNames = new FileNameFinder().getFileNames(project.file('.').absolutePath, render.input)

            if (!matchingFileNames.empty) {
                def outputFile = project.file(render.output)

                def isDirectFileRendering = matchingFileNames.size() == 1 &&
                        !outputFile.directory &&
                        outputFile.name.endsWith(render.format.fileSuffix)

                if (isDirectFileRendering) {
                    addFileToPreparedRenders(preparedRenders, new File(matchingFileNames[0]), outputFile, render.format, false)
                } else {
                    addDirectoryToPreparedRenders(preparedRenders, matchingFileNames, outputFile, render.format)
                }
            } else {
                project.logger.warn("[PlantUml] Warning: ignoring render input: '${render.input}' because no suitable files have been found")
            }
        }

        return preparedRenders
    }

    private static void addFileToPreparedRenders(List<PlantUmlPreparedRender> preparedRenders, File inputFile, File outputFile, FileFormat fileFormat, boolean outputReceivedAsDirectory) {
        assert outputFile.parentFile.exists() || outputFile.parentFile.mkdirs()
        preparedRenders << new PlantUmlPreparedRender(inputFile, outputFile, fileFormat, outputReceivedAsDirectory)
    }

    private static void addDirectoryToPreparedRenders(List<PlantUmlPreparedRender> preparedRenders, List<String> inputFiles, File outputDirectory, FileFormat fileFormat) {
        assert outputDirectory.exists() || outputDirectory.mkdirs(): "Cannot create directory ${outputDirectory.absolutePath}"
        assert outputDirectory.isDirectory():
                "Input ${inputFiles} matches multiple files, but output ${outputDirectory.absolutePath} is no directory"

        inputFiles.each { inputFilePath ->
            def inputFile = new File(inputFilePath)
            def outputFileName = "${inputFile.name.take(inputFile.name.lastIndexOf('.'))}${fileFormat.fileSuffix}"
            def outputFile = new File(outputDirectory, outputFileName)

            addFileToPreparedRenders(preparedRenders, inputFile, outputFile, fileFormat, true)
        }
    }
}
