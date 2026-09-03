/*
 * Copyright (c) 2021-present CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.conventions;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConventionsPluginFunctionalTest {

    @TempDir
    Path projectDir;

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "com.cleanroommc.conventions",
                    "com.cleanroommc.conventions.base",
                    "com.cleanroommc.conventions.license",
                    "com.cleanroommc.conventions.style",
                    "com.cleanroommc.conventions.testing",
                    "com.cleanroommc.conventions.benchmarking",
                    "com.cleanroommc.conventions.publishing",
                    "com.cleanroommc.conventions.mod"
            }
    )
    void appliesToAJavaProject(String pluginId) throws IOException {
        project("id 'java'\n    id '" + pluginId + "'", "");
        assertThat(run("help").getOutput()).contains("BUILD SUCCESSFUL");
    }

    @Test
    void formatJRunsFromThePackedStyleWithoutAnyFileOnDisk() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.style'", "");
        Path source = projectDir.resolve("src/main/java/example/Example.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "package example;\n\npublic class Example {\n            void run() {\n            }\n}\n");

        run("formatJavaApply");

        assertThat(Files.readString(source)).contains("\n    void run() {");
        assertThat(projectDir.resolve("formatj.toml")).doesNotExist();
    }

    @Test
    void aFullBuildLeavesTheProjectDirectoryAlone() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", "");

        run("assemble");

        assertThat(projectDir.resolve("checkstyle.xml")).doesNotExist();
        assertThat(projectDir.resolve("formatj.toml")).doesNotExist();
        assertThat(projectDir.resolve("cliff.toml")).doesNotExist();
        assertThat(projectDir.resolve("LICENSE")).doesNotExist();
        assertThat(projectDir.resolve("HEADER")).doesNotExist();
        assertThat(projectDir.resolve(".editorconfig")).doesNotExist();
        assertThat(projectDir.resolve(".gitattributes")).doesNotExist();
        assertThat(projectDir.resolve(".gitignore")).doesNotExist();
    }

    @Test
    void extractConventionsWritesPackedFiles() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", "");

        run("extractConventions");

        assertThat(projectDir.resolve("checkstyle.xml")).exists();
        assertThat(projectDir.resolve("formatj.toml")).exists();
        assertThat(projectDir.resolve("cliff.toml")).exists();
        assertThat(Files.readString(projectDir.resolve("LICENSE"))).contains("CleanroomMC License");
        assertThat(Files.readString(projectDir.resolve("HEADER"))).contains("CleanroomMC License Version 1.0");
        assertThat(projectDir.resolve(".editorconfig")).exists();
        assertThat(projectDir.resolve(".gitattributes")).exists();
        assertThat(Files.readString(projectDir.resolve(".gitignore"))).contains("# >>> cleanroom-conventions");
        assertThat(run("assemble").getOutput()).doesNotContain("extractConventions");
    }

    @Test
    void extractConventionsMergesTheGitignoreRegion() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", "");
        Files.writeString(projectDir.resolve(".gitignore"), "# >>> cleanroom-conventions\nold/\n# <<< cleanroom-conventions\nmine.iml\n");

        run("extractConventions");

        String gitignore = Files.readString(projectDir.resolve(".gitignore"));
        assertThat(gitignore).contains("mine.iml");
        assertThat(gitignore).contains("# >>> cleanroom-conventions");
        assertThat(gitignore).contains(".gradle/");
        assertThat(gitignore).doesNotContain("old/");
    }

    @Test
    void groupDefaultsToCleanroomMc() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", printGroup());
        assertThat(run("printGroup").getOutput()).contains("group=com.cleanroommc");
    }

    @Test
    void groupCanBeOverriddenAfterThePlugin() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", "group = 'zone.rong'\n\n" + printGroup());
        assertThat(run("printGroup").getOutput()).contains("group=zone.rong");
    }

    @Test
    void jspecifyIsCompileOnly() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", printCompileOnly());
        assertThat(run("printCompileOnly").getOutput()).contains("org.jspecify:jspecify:1.0.0");
    }

    @Test
    void ideaDownloadsSourcesAndJavadoc() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", printIdea());
        String output = run("printIdea").getOutput();
        assertThat(output).contains("sources=true");
        assertThat(output).contains("javadoc=true");
    }

    @Test
    void jarManifestMatchesThePomIdentity() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", printManifest());
        String output = run("jar").getOutput();
        assertThat(output).contains("title=conventions-under-test");
        assertThat(output).contains("vendor=CleanroomMC");
        assertThat(output).contains("vendorId=com.cleanroommc");
        assertThat(output).contains("specVendor=CleanroomMC");
    }

    @Test
    void testsLogPassedSkippedAndFailed() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.testing'", printTestLogging());
        String output = run("printTestLogging").getOutput();
        assertThat(output).contains("PASSED");
        assertThat(output).contains("SKIPPED");
        assertThat(output).contains("FAILED");
    }

    @Test
    void signingStaysOffWithoutKeys() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.publishing'", "");
        String output = run("tasks", "--all").getOutput();
        assertThat(output).doesNotContain("signMavenPublication");
    }

    @Test
    void signingRegistersWhenKeysArePresent() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.publishing'", "");
        property("signingKey = not-a-real-key");
        property("signingPassword = not-a-real-password");
        assertThat(run("tasks", "--all").getOutput()).contains("signMavenPublication");
    }

    @Test
    void settingsPluginKeepsConventionRepositoriesWhenTheProjectAddsMore() throws IOException {
        settingsProject(
                """
                plugins {
                    id 'com.cleanroommc.conventions.settings'
                }
                rootProject.name = 'conventions-under-test'
                """,
                """
                plugins { id 'java' }
                repositories {
                    maven {
                        name = 'Extra'
                        url = 'https://example.invalid'
                    }
                }
                tasks.register('printRepos') {
                    def names = repositories.collect { it.name }.join(',')
                    doLast { println "repos=[$names]" }
                }
                """
        );
        String output = run("printRepos").getOutput();
        assertThat(output).contains("MavenRepo");
        assertThat(output).contains("Cleanroom");
        assertThat(output).contains("Extra");
        assertThat(output.indexOf("MavenRepo")).isLessThan(output.indexOf("Extra"));
        assertThat(output.indexOf("Cleanroom")).isLessThan(output.indexOf("Extra"));
    }

    @Test
    void settingsPluginDoesNotApplyFoojayByDefault() throws IOException {
        settingsProject(printSettings("foojay"), "plugins { id 'java' }\n");
        assertThat(run("help").getOutput()).contains("foojay=false");
    }

    @Test
    void settingsPluginAppliesFoojayWhenProvisionJavaIsOn() throws IOException {
        settingsProject(printSettings("foojay"), "plugins { id 'java' }\n");
        Files.writeString(projectDir.resolve("gradle.properties"), "conventions.provisionJava = true\n");
        assertThat(run("help").getOutput()).contains("foojay=true");
    }

    @Test
    void settingsPluginAddsConventionRepositories() throws IOException {
        settingsProject(
                """
                plugins {
                    id 'com.cleanroommc.conventions.settings'
                }
                rootProject.name = 'conventions-under-test'
                """,
                printRepos()
        );
        String output = run("printRepos").getOutput();
        assertThat(output).contains("MavenRepo");
        assertThat(output).contains("Cleanroom");
    }

    @Test
    void checkstyleWarnsAboutImportedForeignNullness() throws IOException {
        project(
                "id 'java'\n    id 'com.cleanroommc.conventions'",
                """
                dependencies {
                    compileOnly 'com.google.code.findbugs:jsr305:3.0.2'
                }
                """
        );
        Path source = projectDir.resolve("src/main/java/example/Example.java");
        Files.createDirectories(source.getParent());
        Files.writeString(
                source,
                javaSource(
                        """
                        package example;

                        %s

                        public class Example {

                            @Nullable
                            public String name() {
                                return null;
                            }

                        }
                        """.formatted(
                                "import javax.annotation.Nullable;"
                        )
                )
        );
        String output = run("checkstyleMain").getOutput();
        assertThat(output).contains("Prefer org.jspecify.annotations for nullness");
        assertThat(output).contains("BUILD SUCCESSFUL");
    }

    @Test
    void checkstyleAllowsFullyQualifiedForeignNullness() throws IOException {
        project(
                "id 'java'\n    id 'com.cleanroommc.conventions'",
                """
                dependencies {
                    compileOnly 'com.google.code.findbugs:jsr305:3.0.2'
                }
                """
        );
        Path source = projectDir.resolve("src/main/java/example/Example.java");
        Files.createDirectories(source.getParent());
        Files.writeString(
                source,
                javaSource(
                        """
                        package example;

                        public class Example {

                            @javax.annotation.Nullable
                            public String name() {
                                return null;
                            }

                        }
                        """
                )
        );
        assertThat(run("checkstyleMain").getOutput()).contains("BUILD SUCCESSFUL");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void checkstyleAllowsJSpecifyNullness(boolean fqcn) throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", "");
        Path source = projectDir.resolve("src/main/java/example/Example.java");
        Files.createDirectories(source.getParent());
        Files.writeString(
                source,
                javaSource(
                        fqcn
                                ? """
                                package example;

                                public class Example {

                                    @org.jspecify.annotations.Nullable
                                    public String name() {
                                        return null;
                                    }

                                }
                                """
                                : """
                                package example;

                                import org.jspecify.annotations.Nullable;

                                public class Example {

                                    @Nullable
                                    public String name() {
                                        return null;
                                    }

                                }
                                """
                )
        );
        assertThat(run("checkstyleMain").getOutput()).contains("BUILD SUCCESSFUL");
    }

    @Test
    void toolchainDefaultsTo25() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", printToolchain());
        assertThat(run("printToolchain").getOutput()).contains("toolchain=25");
    }

    @Test
    void toolchainHonoursTheJavaMajorProperty() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.base'", printToolchain());
        property("conventions.javaMajor = 21");
        assertThat(run("printToolchain").getOutput()).contains("toolchain=21");
    }

    @Test
    void testingAddsJUnitMockitoAndAssertJ() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.testing'", printTestDependencies());

        String output = run("printTestDependencies").getOutput();

        assertThat(output).contains("org.junit:junit-bom:6.1.3");
        assertThat(output).contains("org.junit.jupiter:junit-jupiter");
        assertThat(output).contains("org.mockito:mockito-core:5.23.0");
        assertThat(output).contains("org.assertj:assertj-bom:3.27.7");
        assertThat(output).contains("org.assertj:assertj-core");
        assertThat(output).contains("org.assertj:assertj-guava");
    }

    @Test
    void testingHonoursTheJUnitVersionProperty() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.testing'", printTestDependencies());
        property("conventions.junitVersion = 5.11.4");
        assertThat(run("printTestDependencies").getOutput()).contains("org.junit:junit-bom:5.11.4");
    }

    @Test
    void benchmarkingIsOptIn() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", printBenchmarking());
        assertThat(run("printBenchmarking").getOutput()).contains("benchmarking=absent");
    }

    @Test
    void benchmarkingCanBeEnabledFromTheAggregatePlugin() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", printBenchmarking());
        property("conventions.benchmarking = true");
        assertThat(run("printBenchmarking").getOutput()).contains("benchmarking=present");
    }

    @Test
    void benchmarkingHonoursTheJmhVersionProperty() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.benchmarking'", printBenchmarkDependencies());
        property("conventions.jmhVersion = 1.36");
        String output = run("printBenchmarkDependencies").getOutput();
        assertThat(output).contains("org.openjdk.jmh:jmh-core:1.36");
        assertThat(output).contains("org.openjdk.jmh:jmh-generator-annprocess:1.36");
    }

    @Test
    void benchmarkRunsJmhWithoutCompilingTests() throws IOException {
        project(
                "id 'java'\n    id 'com.cleanroommc.conventions.benchmarking'",
                """
                tasks.named('benchmark') {
                    args 'example.BenchmarkSmoke', '-wi', '0', '-i', '1', '-f', '1', '-r', '10ms'
                }
                """
        );
        javaFile(
                "src/main/java/example/Subject.java",
                """
                package example;

                public final class Subject {

                    public static int value() {
                        return 42;
                    }

                }
                """
        );
        javaFile(
                "src/benchmark/java/example/BenchmarkSmoke.java",
                """
                package example;

                import org.openjdk.jmh.annotations.Benchmark;

                public class BenchmarkSmoke {

                    @Benchmark
                    public int measure() {
                        return Subject.value();
                    }

                }
                """
        );
        javaFile(
                "src/test/java/example/BrokenTest.java",
                """
                package example;

                public class BrokenTest {
                    this deliberately does not compile
                }
                """
        );

        String output = run("--configuration-cache", "benchmark").getOutput();

        assertThat(output).contains("example.BenchmarkSmoke.measure");
        assertThat(output).doesNotContain(":compileTestJava");
        assertThat(output).doesNotContain(":test");
        assertThat(output).contains("Configuration cache entry stored.");
    }

    @Test
    void publishingCreatesAMavenPublicationForAPlainJavaProject() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.publishing'", printPublications());
        assertThat(run("printPublications").getOutput()).contains("publications=[maven]");
    }

    @Test
    void publishingSkipsTheMavenPublicationForAGradlePluginProject() throws IOException {
        project("id 'java-gradle-plugin'\n    id 'com.cleanroommc.conventions.publishing'", printPublications());
        assertThat(run("printPublications").getOutput()).doesNotContain("maven]");
    }

    @Test
    void publishingSkipsTheMavenPublicationWhenTheGradlePluginIsAppliedAfterTheConventions() throws IOException {
        project("id 'com.cleanroommc.conventions.publishing'\n    id 'java-gradle-plugin'", printPublications());
        assertThat(run("printPublications").getOutput()).contains("publications=[pluginMaven]");
    }

    @Test
    void modPublishingIsOptIn() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", printMods());
        assertThat(run("printMods").getOutput()).contains("mods=absent");
    }

    @Test
    void modPublishingExposesTheModsExtensionWhenEnabled() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", printMods());
        property("conventions.modPublishing = true");
        assertThat(run("printMods").getOutput()).contains("mods=present");
    }

    @Test
    void readingTheModsExtensionRegistersNoPlatform() throws IOException {
        project(
                "id 'java'\n    id 'com.cleanroommc.conventions.mod'",
                """
                conventions.mods { }

                tasks.register('printPlatforms') {
                    def platforms = publishMods.platforms.names.join(',')
                    doLast { println "platforms=[$platforms]" }
                }
                """
        );
        assertThat(run("printPlatforms").getOutput()).contains("platforms=[]");
    }

    @Test
    void configuresTheCurseforgePlatformFromTheDsl() throws IOException {
        project(
                "id 'java'\n    id 'com.cleanroommc.conventions.mod'",
                """
                conventions.mods {
                    curseforge = '123456'
                }

                tasks.register('printPlatforms') {
                    def platforms = publishMods.platforms.names.join(',')
                    doLast { println "platforms=[$platforms]" }
                }
                """
        );
        assertThat(run("printPlatforms").getOutput()).contains("platforms=[curseforge]");
    }

    @Test
    void checkstyleRejectsAJavaFileWithoutTheHeader() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.style'", "");
        Path source = projectDir.resolve("src/main/java/example/Example.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "package example;\n\npublic class Example {\n}\n");
        assertThat(runAndFail("checkstyleMain").getOutput()).contains("Missing a header");
    }

    @Test
    void checkstyleAcceptsAJavaFileWithTheHeader() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.style'", "");
        Path source = projectDir.resolve("src/main/java/example/Example.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, javaSource("package example;\n\npublic class Example {\n}\n"));
        assertThat(run("checkstyleMain").getOutput()).contains("BUILD SUCCESSFUL");
    }

    @Test
    void checkLicenseFailsWhenTheFileIsMissing() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.license'", "");
        assertThat(runAndFail("checkLicense").getOutput()).contains("Missing LICENSE");
    }

    @Test
    void checkLicensePassesWhenTheFileMatches() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.license'", "");
        Files.writeString(projectDir.resolve("LICENSE"), ConventionsFile.LICENSE.read());
        assertThat(run("checkLicense").getOutput()).contains("BUILD SUCCESSFUL");
    }

    @Test
    void checkLicenseFailsWhenTheFileDiffers() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.license'", "");
        Files.writeString(projectDir.resolve("LICENSE"), "MIT\n");
        assertThat(runAndFail("checkLicense").getOutput()).contains("does not match CleanroomMC License Version 1.0");
    }

    @Test
    void publishingDeclaresTheCleanroomLicense() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions.publishing'", "");
        run("generatePomFileForMavenPublication");
        String pom = Files.readString(projectDir.resolve("build/publications/maven/pom-default.xml"));
        assertThat(pom).contains(ConventionsDefaults.LICENSE_NAME);
        assertThat(pom).contains(ConventionsDefaults.LICENSE_URL);
        assertThat(pom).contains(ConventionsDefaults.LICENSE_COMMENTS);
    }

    @Test
    void theConfigurationCacheIsReusedAcrossBuilds() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", "");

        run("--configuration-cache", "assemble");
        BuildResult second = run("--configuration-cache", "assemble");

        assertThat(second.getOutput()).contains("Reusing configuration cache.");
    }

    private static String javaSource(String body) {
        return ConventionsFile.javaHeader() + "\n\n" + body;
    }

    private static String printToolchain() {
        return """
                tasks.register('printToolchain') {
                    def version = java.toolchain.languageVersion.get().asInt()
                    doLast { println "toolchain=$version" }
                }
                """;
    }

    private static String printTestDependencies() {
        return """
                tasks.register('printTestDependencies') {
                    def coordinates = configurations.testImplementation.dependencies.collect {
                        [it.group, it.name, it.version].findAll { part -> part != null }.join(':')
                    }
                    doLast { coordinates.each { println "dependency=$it" } }
                }
                """;
    }

    private static String printBenchmarking() {
        return """
                tasks.register('printBenchmarking') {
                    def sourceSet = sourceSets.findByName('benchmark')
                    def benchmarkTask = tasks.findByName('benchmark')
                    def present = sourceSet != null && benchmarkTask != null ? 'present' : 'absent'
                    doLast { println "benchmarking=$present" }
                }
                """;
    }

    private static String printBenchmarkDependencies() {
        return """
                tasks.register('printBenchmarkDependencies') {
                    def implementation = configurations.benchmarkImplementation.dependencies
                    def annotationProcessor = configurations.benchmarkAnnotationProcessor.dependencies
                    def coordinates = (implementation + annotationProcessor).collect {
                        [it.group, it.name, it.version].findAll { part -> part != null }.join(':')
                    }
                    doLast { coordinates.each { println "dependency=$it" } }
                }
                """;
    }

    private static String printPublications() {
        return """
                tasks.register('printPublications') {
                    def names = publishing.publications.names.join(',')
                    doLast { println "publications=[$names]" }
                }
                """;
    }

    private static String printMods() {
        return """
                tasks.register('printMods') {
                    def present = conventions.extensions.findByName('mods') != null ? 'present' : 'absent'
                    doLast { println "mods=$present" }
                }
                """;
    }

    private static String printGroup() {
        return """
                tasks.register('printGroup') {
                    def value = project.group
                    doLast { println "group=$value" }
                }
                """;
    }

    private static String printCompileOnly() {
        return """
                tasks.register('printCompileOnly') {
                    def coordinates = configurations.compileOnly.dependencies.collect {
                        [it.group, it.name, it.version].findAll { part -> part != null }.join(':')
                    }
                    doLast { coordinates.each { println "dependency=$it" } }
                }
                """;
    }

    private static String printIdea() {
        return """
                tasks.register('printIdea') {
                    def sources = idea.module.downloadSources
                    def javadoc = idea.module.downloadJavadoc
                    doLast {
                        println "sources=$sources"
                        println "javadoc=$javadoc"
                    }
                }
                """;
    }

    private static String printManifest() {
        return """
                tasks.named('jar').configure {
                    doLast {
                        def file = archiveFile.get().asFile
                        def attrs = new java.util.jar.JarFile(file).manifest.mainAttributes
                        println "title=${attrs.getValue('Implementation-Title')}"
                        println "version=${attrs.getValue('Implementation-Version')}"
                        println "vendor=${attrs.getValue('Implementation-Vendor')}"
                        println "vendorId=${attrs.getValue('Implementation-Vendor-Id')}"
                        println "specVendor=${attrs.getValue('Specification-Vendor')}"
                    }
                }
                """;
    }

    private static String printTestLogging() {
        return """
                tasks.register('printTestLogging') {
                    def events = tasks.test.testLogging.events.collect { it.name() }
                    doLast { println "events=$events" }
                }
                """;
    }

    private static String printRepos() {
        return """
                plugins { id 'java' }
                tasks.register('printRepos') {
                    def names = repositories.collect { it.name }.join(',')
                    doLast { println "repos=[$names]" }
                }
                """;
    }

    private static String printSettings(String what) {
        return """
                plugins {
                    id 'com.cleanroommc.conventions.settings'
                }
                rootProject.name = 'conventions-under-test'
                gradle.settingsEvaluated { s ->
                    if ('foojay'.equals('%s')) {
                        println "foojay=${s.pluginManager.hasPlugin('org.gradle.toolchains.foojay-resolver-convention')}"
                    }
                }
                """.formatted(
                what
        );
    }

    private void project(String plugins, String body) throws IOException {
        Files.writeString(
                projectDir.resolve("settings.gradle"),
                """
                plugins {
                    id 'com.cleanroommc.conventions.settings'
                }
                rootProject.name = 'conventions-under-test'
                """
        );
        Files.writeString(projectDir.resolve("build.gradle"), "plugins {\n    " + plugins + "\n}\n\n" + body);
        // Cleanroom Versioning refuses to apply without both, so every conventions consumer has to set them.
        Files.writeString(projectDir.resolve("gradle.properties"), "version = 1.0.0\nversioning.stage = release\n");
    }

    private void settingsProject(String settings, String body) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), settings);
        Files.writeString(projectDir.resolve("build.gradle"), body);
    }

    private void property(String line) throws IOException {
        Path properties = projectDir.resolve("gradle.properties");
        Files.writeString(properties, Files.readString(properties) + line + "\n");
    }

    private void javaFile(String relativePath, String body) throws IOException {
        Path source = projectDir.resolve(relativePath);
        Files.createDirectories(source.getParent());
        Files.writeString(source, javaSource(body));
    }

    private BuildResult run(String... arguments) {
        return GradleRunner.create().withProjectDir(projectDir.toFile()).withPluginClasspath().withArguments(arguments).forwardOutput().build();
    }

    private BuildResult runAndFail(String... arguments) {
        return GradleRunner.create().withProjectDir(projectDir.toFile()).withPluginClasspath().withArguments(arguments).forwardOutput().buildAndFail();
    }

}
