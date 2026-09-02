# Conventions
Cleanroom's Conventions. Here you can find how a Cleanroom's project is to behave.

Contains the following conventional files.

- `checkstyle.xml`
- `formatj.toml`
- `cliff.toml`
- `.editorconfig`
- `.gitattributes`
- `.gitignore`

## Gradle Plugin

Published plugin IDs:

| Plugin ID                                | Purpose                                                        |
|------------------------------------------|----------------------------------------------------------------|
| `com.cleanroommc.conventions`            | Applies every convention plugin                                |
| `com.cleanroommc.conventions.base`       | Versioning, Java, encoding, & reproducible archive conventions |
| `com.cleanroommc.conventions.style`      | Code formatting & Checkstyle conventions                       |
| `com.cleanroommc.conventions.testing`    | JUnit, AssertJ & Mockito conventions                           |
| `com.cleanroommc.conventions.publishing` | Maven & Gradle Plugin Portal publishing conventions            |
| `com.cleanroommc.conventions.mod`        | CurseForge & Modrinth publishing conventions                   |

```groovy filename="build.gradle"
plugins {
    id 'java'
    id 'com.cleanroommc.conventions' version '1.0.0'
}
```

### Properties

All optional, set in `gradle.properties`.

| Property                        | Default    | Behaviour                                       |
|---------------------------------|------------|-------------------------------------------------|
| `conventions.javaMajor`         | `25`       | Java toolchain language version                 |
| `conventions.modPublishing`     | `false`    | Applies the mod conventions                     |
| `conventions.repoUrl`           | git remote | Repository URL used for the POM `url` and `scm` |
| `conventions.checkstyleVersion` | `14.0.0`   | Checkstyle version                              |
| `conventions.junitVersion`      | `6.1.3`    | `org.junit:junit-bom` version                   |
| `conventions.mockitoVersion`    | `5.23.0`   | Mockito version                                 |
| `conventions.assertjVersion`    | `3.27.7`   | `org.assertj:assertj-bom` version               |

> [!IMPORTANT]
> Cleanroom Versioning is applied by the base conventions and refuses to apply without `version`
> and `versioning.stage` (one of `alpha`, `beta`, `rc`, `release`).
> Both are required in every consuming project.

### Base Conventions

- Applies `com.cleanroommc.versioning` gradle plugin.
  - Configures projects to follow Cleanroom's Versioning Conventions.
- Force UTF-8 encoding on ALL `JavaCompile`, `Javadoc` and `Test` tasks.
- Mutes Javadoc's `missing` warnings, everything else in `-Xdoclint` stays on.
- Java toolchain from `conventions.javaMajor`.
- Verifiable rebuilding of artifacts

### Style Conventions

- Applies [ClearSkies](https://github.com/Rongmario/ClearSkies), which expands star imports. No configuration.
- Applies [FormatJ](https://github.com/Rongmario/FormatJ) with `formatj.toml`.
- Applies Checkstyle with `checkstyle.xml`.

The three run in a fixed order, since each one judges what the previous one wrote:
- ClearSkies > FormatJ > Checkstyle.

> [!NOTE]
> FormatJ ships an IntelliJ plugin that reads `formatj.toml`.
>
> Copy [this file](formatj.toml) to the root of the project and use the plugin to perform native formatting.

### Testing Conventions

- `org.junit:junit-bom`, `junit-jupiter` and `junit-platform-launcher`.
- `mockito-core` and `mockito-junit-jupiter`.
- `org.assertj:assertj-bom`, `assertj-core` and `assertj-guava`.
- `useJUnitPlatform()` on every `Test`.

### Publishing Conventions

- Adds the `Cleanroom` Maven repository (`https://maven.cleanroommc.com`)
  - Authenticate with `CleanroomUsername` and `CleanroomPassword`.
- Adds a `sources` jar and a `javadoc` jar.
- Fills in POM defaults on every Maven publication:
  - Name
  - Description
  - Url
  - CleanroomMC organization
  - `scm` connections.
    - Repository URL comes from `conventions.repoUrl` fallback being git upstream remote
    - `gradlePlugin.vcsUrl` for plugin projects.
- Creates a `maven` publication from the `java` component (if there is no existing `maven` publication)
  - Unless the project applies `java-gradle-plugin` (which brings its own `pluginMaven` publication)
- For `java-gradle-plugin` projects: applies `com.gradle.plugin-publish` and `signing`.
  - Signing is required only when `publishPlugins` is one of the requested tasks, and uses `signingKey`/`signingPassword`

- Nothing here writes `cliff.toml`. Only the `git-cliff` CLI reads it, so the release workflow fetches it from this repository at the ref the workflow was called at, unless the project ships its own.

### Mod Publishing

Disabled by default. Enable it in the project's `gradle.properties`:

```properties filename="gradle.properties"
conventions.modPublishing = true
```

This exposes the [mod-publish-plugin's](https://modmuss50.github.io/mod-publish-plugin/) `publishMods` extension. Add either or both distributions:

> [!NOTE]
> The following properties have been applied:
> - Minecraft Version: "1.12.2"
> - Mod Loader: "forge" ("cleanroom" if/when distributions support it)
> - File: output of the "jar" task
> - Max Retries: 5
> - Version: `project.version`
> - Version Type: (Alpha/Beta/Stable) applied via Cleanroom's Versioning module
> - CurseForge Access Token: `CURSEFORGE_TOKEN` environment variable
> - Modrinth Access Token: `MODRINTH_TOKEN` environment variable

> [!TIP]
> Any value can be overridden through the upstream DSL. A distribution is only configured when it is named here. Check out the [plugin's wiki](https://modmuss50.github.io/mod-publish-plugin/)

```groovy filename="build.gradle"
conventions {
    mods {
        curseforge = '123456'
        modrinth = 'abcdef'

        // curseforge { }
        // modrinth { }
        // To configure the upstream DSLs
    }
}
```
