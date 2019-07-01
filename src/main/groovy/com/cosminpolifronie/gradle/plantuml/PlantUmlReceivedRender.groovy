package com.cosminpolifronie.gradle.plantuml

import groovy.transform.Canonical
import net.sourceforge.plantuml.FileFormat

@Canonical
class PlantUmlReceivedRender {
    String input
    String output
    String format
    boolean withMetadata = true

    FileFormat getFormat() {
        def formatString = format ?: output.replaceAll(/.*\./, '')
        try {
            FileFormat.valueOf(formatString.toUpperCase())
        } catch (IllegalArgumentException ignored) {
            throw new PlantUmlException(
                    "Format for rendering '${input}' to '${output}' must be explicitly specified via \"format: 'xxx'\"")
        }
    }
}
