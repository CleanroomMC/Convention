# Convention
Cleanroom's Conventions. Here you can find how a Cleanroom's project is to behave.

Contains the following conventional files.

- `checkstyle.xml`
- `cliff.toml`
- `.editorconfig`
- `.gitattributes`
- `.gitignore`

## Gradle Plugin

### conventions-base

- Applies `com.cleanroommc.versioning` gradle plugin. Which configures projects to follow Cleanroom's Versioning Conventions.
- Force UTF-8 encoding on ALL `JavaCompile` tasks.
- Verifiable rebuilding or artifacts
