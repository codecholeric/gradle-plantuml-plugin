# Gradle PlantUML Plugin

This is a very simple plugin to render a couple of files via [PlantUML](http://plantuml.com/).

## How to use it

Declare the plugin:

```
plugins {
  id 'de.gafertp.plantuml' version '1.0.0'
}

apply plugin: 'de.gafertp.plantuml'
```

Then configure PlantUML files to render:

```
plantUml {
    render input: 'diagrams/some.puml', output: 'some.svg', format: 'svg'
    render input: 'diagrams/some.ditaa', output: 'some.png'
}
```

Note that `format: 'xxx'` is optional and will default to `format: 'png'`.

The plugin adds a custom `plantUml` task:

```
./gradlew plantUml
```