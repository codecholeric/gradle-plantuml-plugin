package de.gafertp.plantuml

import net.sourceforge.plantuml.FileFormat
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PlantUmlPlugin implements Plugin<Project> {
    Logger logger = LoggerFactory.getLogger(PlantUmlPlugin)

    void apply(Project project) {

        def extension = project.extensions.create('plantUml', PlantUmlPluginExtension)

        project.tasks.register('plantUml', PlantUmlTask) {
            logger.warn '''
                WARNING: Maintenance of this plugin has moved on to "com.cosminpolifronie.gradle.plantuml", please use that plugin instead!!
            '''.stripIndent()

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
                addFileToPreparedRenders(preparedRenders, new File(matchingFileNames[0]), outputFile, render.format)
            } else {
                addDirectoryToPreparedRenders(preparedRenders, matchingFileNames, outputFile, render.format)
            }
        }

        return preparedRenders
    }

    private static void addFileToPreparedRenders(List<PlantUmlPreparedRender> preparedRenders, File inputFile, File outputFile, FileFormat fileFormat) {
        assert outputFile.parentFile.exists() || outputFile.parentFile.mkdirs()
        preparedRenders << new PlantUmlPreparedRender(inputFile, outputFile, fileFormat)
    }

    private static void addDirectoryToPreparedRenders(List<PlantUmlPreparedRender> preparedRenders, List<String> inputFiles, File outputDirectory, FileFormat fileFormat) {
        assert outputDirectory.exists() || outputDirectory.mkdirs(): "Cannot create directory ${outputDirectory.absolutePath}"
        assert outputDirectory.isDirectory():
                "Input ${inputFiles} matches multiple files, but output ${outputDirectory.absolutePath} is no directory"

        inputFiles.each { inputFilePath ->
            def inputFile = new File(inputFilePath)
            def outputFileName = "${inputFile.name.take(inputFile.name.lastIndexOf('.'))}${fileFormat.fileSuffix}"
            def outputFile = new File(outputDirectory, outputFileName)

            addFileToPreparedRenders(preparedRenders, inputFile, outputFile, fileFormat)
        }
    }
}
