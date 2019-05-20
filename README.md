# Gradle PlantUML Plugin

This is a plugin that renders diagram files via [PlantUML](http://plantuml.com/).

## Requirements

* [Graphviz](https://www.graphviz.org/download/) (may be needed for rendering certain diagrams, use a version defined [here](http://plantuml.com/graphviz-dot))

## How to use it

Declare the plugin:

```
plugins {
  id 'de.gafertp.plantuml' version '2.1.0'
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

The plugin adds a custom `:plantUml` task:

```
./gradlew :plantUml
```

## Incremental build support

The plugin uses incremental builds. This means that only modified input files will be rendered on consecutive runs (at first run all the files will be rendered again, Gradle has to build its cache). When any of the output files change, all inputs will be rendered again.

## Multithreading support

This plugin renders all the inputs in parallel (using worker threads).

## How to use your own version of PlantUML

To work out of the box, the `gradle-plantuml-plugin` declares a transitive dependency on
`net.sourceforge.plantuml:plantuml:${version}`, where `${version}` is the current version 
released to Maven Central at the time of the plugin release.

You can drop in your own version of PlantUML (provided the API used by the plugin is compatible)
by configuring the plugin within the `buildscript` block:

```
buildscript {
    dependencies {
        classpath('de.gafertp:gradle-plantuml-plugin:2.1.0') {
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

## JBoss Artifactory local repository for forking and local testing

If you want to fork this plugin and extend its functionality, you will also want to test your modifications. For this, you'll have to deploy the plugin to a local artifact repository.

For this you can use a local JFrog Artifactory artifact repository. 

Add the following line to your `plugins` closure in `build.gradle`:
```
// for local testing purposes
id "com.jfrog.artifactory" version '4.9.6'
```

Add the following to `build.gradle`:
```
// for local testing purposes
task sourcesJar(type: Jar, dependsOn: classes) {
    from sourceSets.main.allSource
    archiveClassifier = 'source'
}

// for local testing purposes
artifacts {
    archives sourcesJar
}

// for local testing purposes
publishing {
    repositories {
        mavenLocal()
    }

    publications {
        // used by Artifactory Gradle plugin
        plantUmlPluginJar(MavenPublication) {
            from components.java
            // these have to be specified explicitly because
            // the Artifactory Gradle plugin is not compatible with
            // the java-gradle-plugin that exports jars using Gradle Marker Artifacts
            // Gradle Marker Artifacts are needed to use the new plugins DSL
            // instead of buildscript repository, dependency and apply plugin declarations
            groupId gradlePlugin.plugins.plantUmlPlugin.id
            artifactId gradlePlugin.plugins.plantUmlPlugin.id + ".gradle.plugin"

            artifact(sourcesJar) {
                classifier = 'source'
            }
        }
    }
}

// for local testing purposes
artifactory {
    contextUrl = "${artifactory_contextUrl}"
    publish {
        repository {
            repoKey = "${artifactory_publish_repoKey}"
            username = "${artifactory_user}"
            password = "${artifactory_password}"
            maven = true
        }
        defaults {
            publications('plantUmlPluginJar')
        }
    }
    resolve {
        repository {
            repoKey = "${artifactory_resolve_repoKey}"
            username = "${artifactory_user}"
            password = "${artifactory_password}"
            maven = true
        }
    }
}
```

You have to make/edit your `~/.gradle/gradle.properties` (`~` means your home directory) file and add the following details (considering you are running with default settings and a default Gradle repository initialized):

```
artifactory_user=your_artifactory_user
artifactory_password=your_artifactory_encrypted_password
artifactory_contextUrl=http://localhost:8081/artifactory
artifactory_contextUrl_resolve=http://localhost:8081/artifactory/gradle-dev
artifactory_publish_repoKey=gradle-dev-local
artifactory_resolve_repoKey=gradle-dev
```

Make sure to change the plugin version to something new, so that it doesn't get confused with the official ones:

`build.gradle` in the plugin fork:
```
group = 'de.gafertp.plantuml'
version = '<your_plugin_version>'
```

After this, just run the `:artifactoryPublish` task after building, and the plugin will be automatically published to your local repository.

To use this custom build in your project, just add the following to your `settings.gradle` file:

```
pluginManagement {
    repositories {
        maven {
            url "${artifactory_contextUrl_resolve}"
        }
    }
}
```

It is not necessary to modify your plugins closure in `build.gradle` in the project that uses the plugin. It should look like this:

```
plugins {
	// this is a custom plugin that will be found on a local Artifactory repository
	id 'de.gafertp.plantuml' version '<your_plugin_version>'
}
```