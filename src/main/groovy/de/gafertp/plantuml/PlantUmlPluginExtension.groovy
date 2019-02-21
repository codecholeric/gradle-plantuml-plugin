package de.gafertp.plantuml

class PlantUmlPluginExtension {
    List<PlantUmlReceivedRender> receivedRenders = []

    void render(config) {
        assert config.input != null: 'PlantUML rendering input must be specified via \'input: $fileLocation\''
        assert config.output != null: 'PlantUML rendering output must be specified via \'output: $fileLocation\''

        receivedRenders << new PlantUmlReceivedRender(input: config.input, output: config.output, format: config.format)
    }
}
