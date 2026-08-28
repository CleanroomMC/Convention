package com.cleanroommc.conventions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.testing.Test;

/**
 * Testing Conventions plugin.
 */
public class ConventionsTestingPlugin implements Plugin<Project> {

    private static final String JUNIT_VERSION = "6.1.3";
    private static final String MOCKITO_VERSION = "5.23.0";

    @Override
    public void apply(Project project) {
        // Apply JUnit Platform Launcher, Jupiter, Vintage Engine, Mockito dependencies
        project.getPlugins().withType(JavaPlugin.class, _ -> {
            DependencyHandler dependencies = project.getDependencies();
            String testImplementation = JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME;
            String testRuntimeOnly = JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME;

            dependencies.add(testImplementation, dependencies.platform("org.junit:junit-bom:" + JUNIT_VERSION));
            dependencies.add(testImplementation, "org.junit.jupiter:junit-jupiter");
            dependencies.add(testRuntimeOnly, "org.junit.platform:junit-platform-launcher");

            dependencies.add(testImplementation, "org.mockito:mockito-core:" + MOCKITO_VERSION);
            dependencies.add(testImplementation, "org.mockito:mockito-junit-jupiter:" + MOCKITO_VERSION);
        });

        // Force JUnit Platform on all Tests
        project.getTasks().withType(Test.class).configureEach(Test::useJUnitPlatform);
    }

}
