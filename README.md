# Convention
Cleanroom's Conventions. Here you can find how a Cleanroom's project is to behave.

Contains the following conventional files.

- `checkstyle.xml`
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

### Base Conventions

- Applies `com.cleanroommc.versioning` gradle plugin.
  - Configures projects to follow Cleanroom's Versioning Conventions.
- Force UTF-8 encoding on ALL `JavaCompile` tasks.
- Verifiable rebuilding of artifacts

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
> Any value can be overridden through the upstream DSL. A distribution is only configured when its block is present. Check out the [plugin's wiki](https://modmuss50.github.io/mod-publish-plugin/)

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
