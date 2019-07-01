package com.cosminpolifronie.gradle.plantuml

import groovy.transform.Canonical
import net.sourceforge.plantuml.FileFormat

@Canonical
class PlantUmlPreparedRender implements Serializable {
    File input
    File output
    FileFormat format
    boolean outputReceivedAsDirectory
    boolean withMetadata = true
}
