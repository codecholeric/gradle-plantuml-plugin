package de.gafertp.plantuml

import org.apache.commons.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

class PlantUmlTask extends DefaultTask {
    private final WorkerExecutor workerExecutor

    @Input
    private Map<File, PlantUmlPreparedRender> inputPreparedRenderMap = [:]

    @InputFiles
    Set<File> inputFiles = []

    @OutputDirectories
    Set<File> outputDirectories = []

    void addPreparedRender(PlantUmlPreparedRender preparedRender) {
        inputPreparedRenderMap << [(preparedRender.input): preparedRender]
        inputFiles << preparedRender.input
        outputDirectories << preparedRender.output.parentFile
    }

    @Inject
    PlantUmlTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
    }

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        // local variable that references the private WorkerExecutor above
        // it needs to be referenced here in order to be accessible from a closure
        // see https://stackoverflow.com/a/40957351/6271450
        def localWorkerExecutor = workerExecutor
        def localInputPreparedRenderMap = inputPreparedRenderMap

        if (!inputs.incremental) {
            logger.lifecycle('[PlantUml] Gradle cannot use an incremental build - rendering everything')
            logger.lifecycle("[PlantUml] Deleting all output directories contents: ${outputDirectories.toString()}")
            for (File directory in outputDirectories) {
                FileUtils.cleanDirectory(directory)
            }
        }

        inputs.outOfDate { change ->
            if (localInputPreparedRenderMap.containsKey(change.file)) {
                def preparedRender = localInputPreparedRenderMap[change.file]
                logger.lifecycle("[PlantUml] Rendering file ${preparedRender.input.toString()} to ${preparedRender.output.toString()}")
                localWorkerExecutor.submit(PlantUmlRenderer.class, new Action<WorkerConfiguration>() {
                    @Override
                    void execute(WorkerConfiguration workerConfiguration) {
                        workerConfiguration.setIsolationMode(IsolationMode.NONE)
                        workerConfiguration.params(preparedRender)
                    }
                })
            } else {
                throw new PlantUmlException(
                        "Input file ${change.file.toString()} declared as out of date by Gradle but not declared as an input value by the user"
                )
            }
        }

        inputs.removed { change ->
            def preparedRender = localInputPreparedRenderMap[change.file]
            if (preparedRender.output.exists()) {
                logger.lifecycle("[PlantUml] Deleting output file ${preparedRender.output.toString()}} because of missing input file ${preparedRender.input.toString()}")
                preparedRender.output.delete()
            }
        }
    }
}
