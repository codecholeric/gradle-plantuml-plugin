package com.cosminpolifronie.gradle.plantuml

import net.sourceforge.plantuml.FileFormat
import org.gradle.api.Project

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

class PlantUmlUtils {
    static File tryGetOutputFileForNotExistingInput(Map<String, PlantUmlReceivedRender> inputReceivedRenderMap, Project project, File input) {
        String relativeInputPath = project.relativePath(input).replace('\\', '/')

        if (inputReceivedRenderMap.containsKey(relativeInputPath)) {
            return project.file(inputReceivedRenderMap[relativeInputPath].output)
        }

        for (Map.Entry<String, PlantUmlReceivedRender> mapEntry : inputReceivedRenderMap.entrySet()) {
            PathMatcher globPathMatcher = FileSystems.getDefault().getPathMatcher('glob:' + mapEntry.key)
            Path path = FileSystems.getDefault().getPath(relativeInputPath)
            if (globPathMatcher.matches(path)) {
                File outputFolder = project.file(mapEntry.value.output)
                return project.file(outputFolder.path + '/' + input.name.substring(0, input.name.indexOf('.')) + '.' + mapEntry.value.format.toString().toLowerCase())
            }
        }

        return null
    }

    static List<PlantUmlPreparedRender> prepareRenders(Project project, List<PlantUmlReceivedRender> receivedRenders) {
        List<PlantUmlPreparedRender> preparedRenders = []

        receivedRenders.each { render ->
            def matchingFileNames = new FileNameFinder().getFileNames(project.file('.').absolutePath, render.input)

            if (!matchingFileNames.empty) {
                def outputFile = project.file(render.output)

                def isDirectFileRendering = matchingFileNames.size() == 1 &&
                        !outputFile.directory &&
                        outputFile.name.endsWith(render.format.fileSuffix)

                if (isDirectFileRendering) {
                    addFileToPreparedRenders(preparedRenders, new File(matchingFileNames[0]), outputFile, render.format, false, render.withMetadata)
                } else {
                    addDirectoryToPreparedRenders(preparedRenders, matchingFileNames, outputFile, render.format, render.withMetadata)
                }
            } else {
                project.logger.warn("[PlantUml] Warning: ignoring render input: '${render.input}' because no suitable files have been found")
            }
        }

        return preparedRenders
    }

    static void addFileToPreparedRenders(List<PlantUmlPreparedRender> preparedRenders, File inputFile, File outputFile, FileFormat fileFormat, boolean outputReceivedAsDirectory, boolean withMetadata) {
        assert outputFile.parentFile.exists() || outputFile.parentFile.mkdirs()
        preparedRenders << new PlantUmlPreparedRender(inputFile, outputFile, fileFormat, outputReceivedAsDirectory, withMetadata)
    }

    static void addDirectoryToPreparedRenders(List<PlantUmlPreparedRender> preparedRenders, List<String> inputFiles, File outputDirectory, FileFormat fileFormat, boolean withMetadata) {
        assert outputDirectory.exists() || outputDirectory.mkdirs(): "Cannot create directory ${outputDirectory.absolutePath}"
        assert outputDirectory.isDirectory():
                "Input ${inputFiles} matches multiple files, but output ${outputDirectory.absolutePath} is no directory"

        inputFiles.each { inputFilePath ->
            def inputFile = new File(inputFilePath)
            def outputFileName = "${inputFile.name.take(inputFile.name.lastIndexOf('.'))}${fileFormat.fileSuffix}"
            def outputFile = new File(outputDirectory, outputFileName)

            addFileToPreparedRenders(preparedRenders, inputFile, outputFile, fileFormat, true, withMetadata)
        }
    }
}
