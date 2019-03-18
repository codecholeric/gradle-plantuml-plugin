package de.gafertp.plantuml

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

        for (Map.Entry<String, PlantUmlReceivedRender> mapEntry: inputReceivedRenderMap.entrySet()) {
            PathMatcher globPathMatcher = FileSystems.getDefault().getPathMatcher('glob:' + mapEntry.key)
            Path path = FileSystems.getDefault().getPath(relativeInputPath)
            if (globPathMatcher.matches(path)) {
                File outputFolder = project.file(mapEntry.value.output)
                return project.file(outputFolder.path + '/' + input.name.substring(0, input.name.indexOf('.')) + '.' + mapEntry.value.format)
            }
        }

        return null
    }
}
