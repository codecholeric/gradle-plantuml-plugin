package de.gafertp

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlantUmlPlugin implements Plugin<Project> {
    void apply(Project project) {

        def extension = project.extensions.create('plantUml', PlantUmlExtension)

        def render = { File from, File to, FileFormat format ->
            to.withOutputStream { out ->
                new SourceStringReader(from.text).generateImage(out, new FileFormatOption(format));
            }
        }

        project.task('plantUml') {
            doLast {
                extension.renderings.each {
                    render(project.file(it.input), project.file(it.output), FileFormat.valueOf(it.format.toUpperCase()))
                }
            }
        }
    }
}
