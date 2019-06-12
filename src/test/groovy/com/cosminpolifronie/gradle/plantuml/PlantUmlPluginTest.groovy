package com.cosminpolifronie.gradle.plantuml

import net.sourceforge.plantuml.FileFormat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory
import org.junitpioneer.jupiter.TempDirectory.TempDir

import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

@ExtendWith(TempDirectory)
class PlantUmlPluginTest {
    File rootDir
    File buildFile
    File diagramDir

    File firstPumlFile
    File secondPumlFile
    File nestedPumlFile

    File ditaaFile

    @BeforeEach
    void setup(@TempDir Path tempDir) {
        rootDir = tempDir.toFile()
        buildFile = new File(rootDir, 'build.gradle')
        diagramDir = new File(rootDir, 'diagrams')
        diagramDir.mkdirs()

        firstPumlFile = new File(diagramDir, 'first.puml')
        secondPumlFile = new File(diagramDir, 'second.puml')
        nestedPumlFile = new File(new File(diagramDir, 'nested'), 'third.puml')
        nestedPumlFile.parentFile.mkdirs()
        ditaaFile = new File(diagramDir, 'some.ditaa')

        buildFile << """
            plugins {
                id 'com.cosminpolifronie.gradle.plantuml'
            }
        """

        [firstPumlFile, secondPumlFile, nestedPumlFile].each {
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

        def generatedDiagram = new File(rootDir, 'output/sub/puml.svg')
        assert generatedDiagram.exists(): 'Rendered diagram exists'
        assert generatedDiagram.isFile(): 'Rendered diagram is a file'
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
    void renders_glob_pattern() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/**/*.puml', output: 'output/sub', format: 'svg'
            }
        """

        executePluginTask()

        assertOutputsExist([firstPumlFile, secondPumlFile, nestedPumlFile], FileFormat.SVG)
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

        def result = plantUmlTaskExecution().build()
        assert result.output.contains('[PlantUml] Warning: ignoring render input: \'diagrams/*.notthere\' because no suitable files have been found')
        assert !new File(rootDir, 'output/sub').exists()
    }

    @Test
    void renders_single_matching_file_in_folder() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/f*.puml', output: 'output/sub', format: 'png'
            }
        """

        executePluginTask()

        assert new File(rootDir, 'output/sub').list().toList().contains('first.png'): 'Output directory is empty'
    }

    @Test
    void renders_single_matching_file_to_file_name() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/f*.puml', output: 'output/explicit.png', format: 'png'
            }
        """

        executePluginTask()

        assert new File(rootDir, 'output').list().toList().contains('explicit.png'): 'Output directory is empty'
    }

    @Test
    void rejects_multiple_files_if_no_format_is_specified() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*', output: 'output/sub'
            }
        """

        def result = plantUmlTaskExecution().buildAndFail()

        assert result.task(':plantUml') == null
        assert result.output.toLowerCase().contains('must be explicitly specified via "format: \'xxx\'"')
    }

    @Test
    void returns_up_to_date_on_same_inputs_outputs() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/f*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlTaskExecution().build()
        assert result.task(':plantUml').outcome == SUCCESS

        def result2 = plantUmlTaskExecution().build()
        assert result2.task(':plantUml').outcome == UP_TO_DATE
        assert !result2.output.contains('[PlantUml] Gradle cannot use an incremental build - rendering everything')
    }

    @Test
    void returns_out_of_date_on_different_inputs() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/f*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlTaskExecution().build()
        assert result.task(':plantUml').outcome == SUCCESS

        firstPumlFile.write("""
            @startuml
            Bob -> Alice : hello2
            @enduml
        """)

        def result2 = plantUmlTaskExecution().build()
        assert result2.task(':plantUml').outcome == SUCCESS
    }

    @Test
    void returns_out_of_date_on_different_outputs() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/f*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlTaskExecution().build()
        assert result.task(':plantUml').outcome == SUCCESS

        def outputFile = new File(rootDir, 'output/sub').listFiles()[0]
        outputFile << 'modifications'

        def result2 = plantUmlTaskExecution().build()
        assert result2.task(':plantUml').outcome == SUCCESS
        assert result2.output.contains('[PlantUml] Gradle cannot use an incremental build - rendering everything')
    }

    @Test
    void returns_out_of_date_on_deleted_input_and_does_not_generate_all() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlTaskExecution().build()
        assert result.task(':plantUml').outcome == SUCCESS

        firstPumlFile.delete()

        def result2 = plantUmlTaskExecution().build()
        assert result2.task(':plantUml').outcome == SUCCESS
        assert !result2.output.contains('[PlantUml] Gradle cannot use an incremental build - rendering everything')
    }

    @Test
    void deletes_output_when_input_deleted() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlTaskExecution().build()
        assert result.task(':plantUml').outcome == SUCCESS

        firstPumlFile.delete()

        def result2 = plantUmlTaskExecution().build()
        assert result2.task(':plantUml').outcome == SUCCESS
        assert result2.output.contains('[PlantUml] Deleting output file')
    }
    
    @Test
    void plantuml_io() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlIOTaskExecution().build()
        assert result.task(':plantUmlIO').outcome == SUCCESS
        assert result.output.contentEquals('diagrams/first.puml,output/sub/first.png\r\ndiagrams/second.puml,output/sub/second.png\r\n')
    }

    @Test
    void plantuml_io_empty() {
        buildFile << """
            plantUml {
            }
        """

        def result = plantUmlIOTaskExecution().build()
        assert result.task(':plantUmlIO').outcome == SUCCESS
        assert result.output.contentEquals('')
    }

    @Test
    void plantuml_output_for_input() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlOutputForInputTaskExecution("${diagramDir.name}/abc.puml").build()
        assert result.task(':plantUmlOutputForInput').outcome == SUCCESS
        assert result.output.contentEquals('output/sub/abc.png\r\n')
    }

    @Test
    void plantuml_output_for_input_empty() {
        buildFile << """
            plantUml {
            }
        """

        def result = plantUmlOutputForInputTaskExecution("${diagramDir.name}/abc.puml").build()
        assert result.task(':plantUmlOutputForInput').outcome == SUCCESS
        assert result.output.contentEquals('')
    }

    @Test
    void plantuml_output_for_input_existing() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlOutputForInputTaskExecution("${diagramDir.name}/first.puml").build()
        assert result.task(':plantUmlOutputForInput').outcome == SUCCESS
        assert result.output.contentEquals('output/sub/first.png\r\n')
    }

    @Test
    void plantuml_output_for_input_no_path() {
        buildFile << """
            plantUml {
                render input: '${diagramDir.name}/*.puml', output: 'output/sub', format: 'png'
            }
        """

        def result = plantUmlOutputForInputTaskExecutionBroken().buildAndFail()
        assert result.output.contains('This task has to be run with the --path option set. Usage: ./gradlew :plantUmlOutputForInput --path="your_path_here"')
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

    private GradleRunner plantUmlIOTaskExecution() {
        GradleRunner.create()
                .withProjectDir(buildFile.parentFile)
                .withArguments('plantUmlIO', '-q')
                .withPluginClasspath()
    }

    private GradleRunner plantUmlOutputForInputTaskExecution(String path) {
        GradleRunner.create()
                .withProjectDir(buildFile.parentFile)
                .withArguments('plantUmlOutputForInput', "--path=\'${path}\'", '-q')
                .withPluginClasspath()
    }

    private GradleRunner plantUmlOutputForInputTaskExecutionBroken() {
        GradleRunner.create()
                .withProjectDir(buildFile.parentFile)
                .withArguments('plantUmlOutputForInput', '-q')
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
