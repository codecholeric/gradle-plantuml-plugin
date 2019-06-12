package com.cosminpolifronie.gradle.plantuml

class PlantUmlPluginExtension {
    List<PlantUmlReceivedRender> receivedRenders = []

    void render(config) {
        assert config.input != null: 'PlantUML rendering input must be specified via \'input: $fileLocation\''
        assert config.output != null: 'PlantUML rendering output must be specified via \'output: $fileLocation\''

        String input = config.input.replace('\\', '/')
        String output = config.output.replace('\\', '/')
        String format = config.format

        receivedRenders << new PlantUmlReceivedRender(input: input, output: output, format: format)
    }
}
