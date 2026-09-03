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

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.toolchain.JavaToolchainService;

/**
 * Benchmarking Conventions plugin.
 */
public class ConventionsBenchmarkingPlugin implements Plugin<Project> {

    private static final String BENCHMARK_SOURCE_SET_NAME = "benchmark";
    private static final String JMH_VERSION = "1.37";

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, _ -> configureBenchmarking(project));
    }

    private void configureBenchmarking(Project project) {
        JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSetContainer sourceSets = java.getSourceSets();
        NamedDomainObjectProvider<SourceSet> main = sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME);
        NamedDomainObjectProvider<SourceSet> benchmark = sourceSets.register(BENCHMARK_SOURCE_SET_NAME, sourceSet -> {
            FileCollection mainOutput = project.files(main.map(SourceSet::getOutput));
            sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(mainOutput));
            sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(mainOutput));
        });

        ConfigurationContainer configurations = project.getConfigurations();
        String jmh = ConventionsProperty.JMH_VERSION.get(project, JMH_VERSION);
        DependencyHandler dependencies = project.getDependencies();
        benchmark.configure(sourceSet -> {
            extend(configurations, sourceSet.getImplementationConfigurationName(), JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
            extend(configurations, sourceSet.getCompileOnlyConfigurationName(), JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME);
            extend(configurations, sourceSet.getRuntimeOnlyConfigurationName(), JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME);
            dependencies.add(sourceSet.getImplementationConfigurationName(), "org.openjdk.jmh:jmh-core:" + jmh);
            dependencies.add(sourceSet.getAnnotationProcessorConfigurationName(), "org.openjdk.jmh:jmh-generator-annprocess:" + jmh);
        });

        JavaToolchainService toolchains = project.getExtensions().getByType(JavaToolchainService.class);
        TaskContainer tasks = project.getTasks();
        tasks.register(BENCHMARK_SOURCE_SET_NAME, JavaExec.class, task -> {
            task.setGroup("benchmark");
            task.setDescription("Runs the JMH benchmark suite.");
            task.getMainClass().set("org.openjdk.jmh.Main");
            task.setClasspath(project.files(benchmark.map(SourceSet::getRuntimeClasspath)));
            task.getJavaLauncher().set(toolchains.launcherFor(java.getToolchain()));
        });
    }

    @SuppressWarnings("unchecked")
    private void extend(ConfigurationContainer configurations, String childName, String parentName) {
        configurations.named(childName).configure(child -> child.extendsFrom(configurations.named(parentName)));
    }

}
