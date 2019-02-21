package de.gafertp.plantuml

import groovy.transform.Canonical
import net.sourceforge.plantuml.FileFormat

@Canonical
class PlantUmlPreparedRender {
    File input
    File output
    FileFormat format
}
