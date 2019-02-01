package de.gafertp

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlantUmlPlugin implements Plugin<Project> {
    void apply(Project project) {

        def extension = project.extensions.create('plantUml', PlantUmlExtension)

        project.task('plantUml') {
            doLast {
                extension.renderings.each { rendering ->
                    def matchingFileNames = new FileNameFinder().getFileNames(project.file('.').absolutePath, rendering.input)
                    def outputFile = project.file(rendering.output)

                    def isDirectFileRendering = matchingFileNames.size() == 1 &&
                            !outputFile.directory &&
                            outputFile.name.endsWith(rendering.format.fileSuffix)

                    if (isDirectFileRendering) {
                        render(new File(matchingFileNames[0]), outputFile, rendering.format)
                    } else {
                        renderAll(matchingFileNames, outputFile, rendering.format)
                    }
                }
            }
        }
    }

    private static void renderAll(List<String> inputFilePaths, File outputFolder, FileFormat format) {
        assert outputFolder.exists() || outputFolder.mkdirs(): "Cannot create directory ${outputFolder.absolutePath}"
        assert outputFolder.isDirectory():
                "Input ${inputFilePaths} matches multiple files, but output ${outputFolder.absolutePath} is no directory"

        inputFilePaths.each { inputFilePath ->
            def inputFile = new File(inputFilePath)
            def outputFileName = "${inputFile.name.take(inputFile.name.lastIndexOf('.'))}${format.fileSuffix}"
            def outputFile = new File(outputFolder, outputFileName)
            render(inputFile, outputFile, format)
        }
    }

    private static void render(File from, File to, FileFormat format) {
        assert to.parentFile.exists() || to.parentFile.mkdirs(): "Cannot create directory ${to.parentFile.absolutePath}"

        to.withOutputStream { out ->
            new SourceStringReader(from.text).generateImage(out, new FileFormatOption(format))
        }
        println "Rendered diagram from ${from.absolutePath} to ${to.absolutePath}"
    }
}
