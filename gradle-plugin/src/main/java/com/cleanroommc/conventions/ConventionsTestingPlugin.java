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
    private static final String ASSERTJ_VERSION = "3.27.7";

    @Override
    public void apply(Project project) {
        // Apply JUnit Platform Launcher, Jupiter, Mockito, AssertJ dependencies
        project.getPlugins().withType(JavaPlugin.class, _ -> {
            DependencyHandler dependencies = project.getDependencies();
            String testImplementation = JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME;
            String testRuntimeOnly = JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME;
            String junit = ConventionsProperty.JUNIT_VERSION.get(project, JUNIT_VERSION);
            String mockito = ConventionsProperty.MOCKITO_VERSION.get(project, MOCKITO_VERSION);
            String assertj = ConventionsProperty.ASSERTJ_VERSION.get(project, ASSERTJ_VERSION);

            dependencies.add(testImplementation, dependencies.platform("org.junit:junit-bom:" + junit));
            dependencies.add(testImplementation, "org.junit.jupiter:junit-jupiter");
            dependencies.add(testRuntimeOnly, "org.junit.platform:junit-platform-launcher");

            dependencies.add(testImplementation, "org.mockito:mockito-core:" + mockito);
            dependencies.add(testImplementation, "org.mockito:mockito-junit-jupiter:" + mockito);

            dependencies.add(testImplementation, dependencies.platform("org.assertj:assertj-bom:" + assertj));
            dependencies.add(testImplementation, "org.assertj:assertj-core");
            dependencies.add(testImplementation, "org.assertj:assertj-guava");
        });

        // Force JUnit Platform on all Tests
        project.getTasks().withType(Test.class).configureEach(Test::useJUnitPlatform);
    }

}
