package com.cleanroommc.conventions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Base Conventions plugin.
 */
public class ConventionsBasePlugin implements Plugin<Project> {

    public static final String EXTENSION = "convention";

    @Override
    public void apply(Project project) {
        // Apply Cleanroom Versioning
        project.getPluginManager().apply("com.cleanroommc.versioning");
        // UTF-8 Encoding on all JavaCompile tasks
        project.getTasks().withType(JavaCompile.class).configureEach(task -> task.getOptions().setEncoding("UTF-8"));
        // Verifiable rebuilds
        project.getTasks().withType(AbstractArchiveTask.class).configureEach(task -> {
            task.setPreserveFileTimestamps(false);
            task.setReproducibleFileOrder(true);
        });
    }

}
