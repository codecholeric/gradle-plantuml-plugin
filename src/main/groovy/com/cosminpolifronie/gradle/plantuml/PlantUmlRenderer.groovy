package com.cosminpolifronie.gradle.plantuml

import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

import javax.inject.Inject

class PlantUmlRenderer implements Runnable {
    private final PlantUmlPreparedRender preparedRender

    @Inject
    PlantUmlRenderer(PlantUmlPreparedRender preparedRender) {
        this.preparedRender = preparedRender
    }

    @Override
    void run() {
        preparedRender.output.withOutputStream { out ->
            new SourceStringReader(preparedRender.input.text).outputImage(out, new FileFormatOption(preparedRender.format))
        }
    }
}
