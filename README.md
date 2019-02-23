# Gradle PlantUML Plugin

This is a very simple plugin to render a couple of files via [PlantUML](http://plantuml.com/).

## How to use it

Declare the plugin:

```
plugins {
  id 'de.gafertp.plantuml' version '1.2.0'
}

apply plugin: 'de.gafertp.plantuml'
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

## How to use your own version of PlantUML

To work out of the box, the `gradle-plantuml-plugin` declares a transitive dependency on
`net.sourceforge.plantuml:plantuml:${version}`, where `${version}` is the current version 
released to Maven Central at the time of the plugin release.

You can drop in your own version of PlantUML (provided the API used by the plugin is compatible)
by configuring the plugin within the `buildscript` block:

```
buildscript {
    dependencies {
        classpath('de.gafertp:gradle-plantuml-plugin:1.2.0') {
            exclude group: 'net.sourceforge.plantuml', module: 'plantuml'
        }
        classpath "net.sourceforge.plantuml:plantuml:${myVersion}"
    }
}

apply plugin: 'de.gafertp.plantuml'
```

Alternatively use a local JAR file:

```
classpath files('libs/plantuml-any.jar')
```