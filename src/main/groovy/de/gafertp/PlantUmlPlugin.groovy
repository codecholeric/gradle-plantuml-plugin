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

                    if (matchingFileNames.size() == 1) {
                        render(new File(matchingFileNames[0]), outputFile, rendering.format)
                    } else {
                        renderAll(matchingFileNames, outputFile, rendering.format)
                    }
                }
            }
        }
    }

    private static File outputFileAccordingToInputFile(File inputFile, File outputFolder, FileFormat format) {
        return new File(outputFolder, "${inputFile.name.take(inputFile.name.lastIndexOf('.'))}${format.fileSuffix}")
    }

    private static void renderAll(List<String> inputFilePaths, File outputFolder, FileFormat format) {
        assert outputFolder.exists() || outputFolder.mkdirs(): "Cannot create directory ${outputFolder.absolutePath}"
        assert outputFolder.isDirectory():
                "Input ${inputFilePaths} matches multiple files, but output ${outputFolder.absolutePath} is no directory"

        inputFilePaths.each { inputFilePath ->
            def inputFile = new File(inputFilePath)
            def outputFile = outputFileAccordingToInputFile(inputFile, outputFolder, format)
            render(inputFile, outputFile, format)
        }
    }

    private static void render(File from, File to, FileFormat format) {
        File theTo = to.name.endsWith(".${format.fileSuffix}")
                      ? to
                      : outputFileAccordingToInputFile(from, to, format)

        assert theTo.parentFile.exists() || theTo.parentFile.mkdirs(): "Cannot create directory ${theTo.parentFile.absolutePath}"

        theTo.withOutputStream { out ->
            new SourceStringReader(from.text).generateImage(out, new FileFormatOption(format))
        }
        println "Rendered diagram from ${from.absolutePath} to ${theTo.absolutePath}"
    }
}
