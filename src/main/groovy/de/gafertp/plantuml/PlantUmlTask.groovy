package de.gafertp.plantuml

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

class PlantUmlTask extends DefaultTask {
    private final WorkerExecutor workerExecutor

    @Input
    Map<File, PlantUmlPreparedRender> inputPreparedRenderMap = [:]

    @InputFiles
    List<File> inputFiles = []

    @OutputFiles
    List<File> outputFiles = []

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

        if (!inputs.incremental) {
            project.delete(outputFiles)
        }

        inputs.outOfDate { change ->
            if (inputPreparedRenderMap.containsKey(change.file)) {
                def preparedRender = inputPreparedRenderMap[change.file]
                localWorkerExecutor.submit(PlantUmlRenderer.class, new Action<WorkerConfiguration>() {
                    @Override
                    void execute(WorkerConfiguration workerConfiguration) {
                        workerConfiguration.setIsolationMode(IsolationMode.NONE)
                        workerConfiguration.params(preparedRender)
                    }
                })
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
