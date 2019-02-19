package de.gafertp.plantuml

import groovy.transform.Canonical
import net.sourceforge.plantuml.FileFormat

@Canonical
class PlantUmlRendering {
    String input
    String output
    String format

    FileFormat getFormat() {
        def formatString = format ?: output.replaceAll(/.*\./, '')
        try {
            FileFormat.valueOf(formatString.toUpperCase())
        } catch (IllegalArgumentException ignored) {
            throw new PlantUmlRenderingException(
                    "Format for rendering '${input}' to '${output}' must be explicitly specified via \"format: 'xxx'\"")
        }
    }
}
