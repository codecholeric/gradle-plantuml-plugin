package de.gafertp

import net.sourceforge.plantuml.FileFormat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory
import org.junitpioneer.jupiter.TempDirectory.TempDir

import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@ExtendWith(TempDirectory)
class PlantUmlPluginTest {
    File rootDir
    File buildFile
    File diagramDir

    File firstPumlFile
    File secondPumlFile

    File ditaaFile

    @BeforeEach
    void setup(@TempDir Path tempDir) {
        rootDir = tempDir.toFile()
        buildFile = new File(rootDir, 'build.gradle')
        diagramDir = new File(rootDir, 'diagrams')
        diagramDir.mkdirs()

        firstPumlFile = new File(diagramDir, 'first.puml')
        secondPumlFile = new File(diagramDir, 'second.puml')
        ditaaFile = new File(diagramDir, 'some.ditaa')

        buildFile << """
            plugins {
                id 'de.gafertp.plantuml'
            }
        """

        [firstPumlFile, secondPumlFile].each {
            it << """
            @startuml
            Bob -> Alice : hello
            @enduml
        """
        }

        ditaaFile << """
            @startditaa
            foo -----> bar
            @endditaa
        """
    }

    @Test
    void renders_single_file() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/${firstPumlFile.name}', output: 'output/sub/puml.svg'
            }
        """

        executePluginTask()

        assert new File(rootDir, 'output/sub/puml.svg').exists(): 'Rendered diagram exists'
    }

    @Test
    void renders_multiple_files_of_same_type() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'svg'
            }
        """

        executePluginTask()

        assertOutputsExist([firstPumlFile, secondPumlFile], FileFormat.SVG)
    }

    @Test
    void renders_multiple_files_of_different_type() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*', output: 'output/sub', format: 'png'
            }
        """

        executePluginTask()

        assertOutputsExist([firstPumlFile, secondPumlFile, ditaaFile], FileFormat.PNG)
    }

    @Test
    void renders_zero_matching_files() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.notthere', output: 'output/sub', format: 'png'
            }
        """

        executePluginTask()

        assert new File(rootDir, 'output/sub').list().toList().isEmpty(): 'Output directory is empty'
    }

    @Test
    void rejects_multiple_files_if_no_format_is_specified() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*', output: 'output/sub'
            }
        """

        def result = plantUmlTaskExecution().buildAndFail()

        assert result.task(':plantUml').outcome == FAILED
        assert result.output.toLowerCase().contains('must be explicitly specified via "format: \'xxx\'"')
    }

    private BuildResult executePluginTask() {
        def result = plantUmlTaskExecution().build()
        assert result.task(':plantUml').outcome == SUCCESS
        result
    }

    private GradleRunner plantUmlTaskExecution() {
        GradleRunner.create()
                    .withProjectDir(buildFile.parentFile)
                    .withArguments('plantUml')
                    .withPluginClasspath()
    }

    void assertOutputsExist(List<File> diagramFiles, FileFormat format) {
        diagramFiles.each { assertOutputExists(it, format) }
    }

    private void assertOutputExists(File diagramFile, FileFormat format) {
        def expectedOutputName = diagramFile.name.replaceAll(/\.[^.]*/, format.fileSuffix)
        def expectedOutput = new File(rootDir, "output/sub/${expectedOutputName}")
        assert expectedOutput.exists(): 'Rendered diagram exists'
    }
}
