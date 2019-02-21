# Gradle PlantUML Plugin

This is a very simple plugin to render a couple of files via [PlantUML](http://plantuml.com/).

## How to use it

Declare the plugin:

```
plugins {
  id 'de.gafertp.plantuml' version '2.0.0'
}
```

Then configure PlantUML files to render:

```
plantUml {
    render input: 'diagrams/some.puml', output: 'some.svg', format: 'svg'
    render input: 'diagrams/some.ditaa', output: 'some.png'
    render input: 'diagrams/release/*.puml', output: "${project.buildDir.absolutePath}/release", format: 'png'
}
```

Input can either be a concrete file name or a glob pattern (e.g. `diagrams/**/*.puml`). Output can
either be a concrete file name or a folder (in which case output file names will match the input file
names with a different ending).

Note that `format: 'xxx'` is optional, if and only if

* the target is a single file
* the target has a known image file ending like `svg` or `png`

The plugin adds a custom `plantUml` task:

```
./gradlew plantUml
```