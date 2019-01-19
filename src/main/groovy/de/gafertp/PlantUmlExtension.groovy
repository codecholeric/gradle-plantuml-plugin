package de.gafertp

class PlantUmlExtension {
    List<PlantUmlRendering> renderings = []

    void render(config) {
        assert config.input != null: 'PlantUML rendering input must be specified via \'input: $fileLocation\''
        assert config.output != null: 'PlantUML rendering output must be specified via \'output: $fileLocation\''

        renderings << new PlantUmlRendering(input: config.input, output: config.output, format: config.format ?: 'png')
    }
}
