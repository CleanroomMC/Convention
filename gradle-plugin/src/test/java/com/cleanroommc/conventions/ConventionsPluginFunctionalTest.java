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
                    "com.cleanroommc.conventions.style",
                    "com.cleanroommc.conventions.testing",
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
    void theConfigurationCacheIsReusedAcrossBuilds() throws IOException {
        project("id 'java'\n    id 'com.cleanroommc.conventions'", "");

        run("--configuration-cache", "assemble");
        BuildResult second = run("--configuration-cache", "assemble");

        assertThat(second.getOutput()).contains("Reusing configuration cache.");
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

    private void project(String plugins, String body) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'conventions-under-test'\n");
        Files.writeString(projectDir.resolve("build.gradle"), "plugins {\n    " + plugins + "\n}\n\n" + body);
        // Cleanroom Versioning refuses to apply without both, so every conventions consumer has to set them.
        Files.writeString(projectDir.resolve("gradle.properties"), "version = 1.0.0\nversioning.stage = release\n");
    }

    private void property(String line) throws IOException {
        Path properties = projectDir.resolve("gradle.properties");
        Files.writeString(properties, Files.readString(properties) + line + "\n");
    }

    private BuildResult run(String... arguments) {
        return GradleRunner.create().withProjectDir(projectDir.toFile()).withPluginClasspath().withArguments(arguments).forwardOutput().build();
    }

}
