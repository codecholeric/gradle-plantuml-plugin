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
        // TODO: the following can be simplified when/if the following pull request gets accepted
        // https://github.com/plantuml/plantuml/pull/220
        // all below becomes
        /*
        preparedRender.output.withOutputStream { out ->
            new SourceStringReader(preparedRender.input.text).outputImage(out, new FileFormatOption(preparedRender.format, preparedRender.withMetadata))
        }
        */

        def fileFormatOption = new FileFormatOption(preparedRender.format)

        if (!preparedRender.withMetadata) {
            fileFormatOption.hideMetadata()
        }

        preparedRender.output.withOutputStream { out ->
            new SourceStringReader(preparedRender.input.text).outputImage(out, fileFormatOption)
        }
    }
}
