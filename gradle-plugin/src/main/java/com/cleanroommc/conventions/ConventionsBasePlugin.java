package com.cleanroommc.conventions;

import com.cleanroommc.versioning.gradle.CleanroomVersioningPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * Base Conventions plugin.
 */
public class ConventionsBasePlugin implements Plugin<Project> {

    private static final String DEFAULT_JAVA_VERSION = "25";

    @Override
    public void apply(Project project) {
        PluginManager plugins = project.getPluginManager();
        ExtensionContainer extensions = project.getExtensions();
        TaskContainer tasks = project.getTasks();

        ConventionsExtension.register(project);

        // Apply Cleanroom Versioning
        plugins.apply(CleanroomVersioningPlugin.class);

        // UTF-8 Encoding on all JavaCompile, Javadoc, Test tasks
        tasks.withType(JavaCompile.class).configureEach(task -> task.getOptions().setEncoding("UTF-8"));
        tasks.withType(Test.class).configureEach(task -> task.setDefaultCharacterEncoding("UTF-8"));
        tasks.withType(Javadoc.class).configureEach(task -> {
            task.getOptions().setEncoding("UTF-8");
            // Muted Javadocs 'missing' group, '-quiet' is filler as the option takes no argument
            ((CoreJavadocOptions) task.getOptions()).addStringOption("Xdoclint:all,-missing", "-quiet");
        });

        // Gets "conventions.javaMajor" and sets Java toolchain to it
        project.getPlugins()
                .withType(
                        JavaPlugin.class,
                        _ -> extensions.getByType(JavaPluginExtension.class)
                                .getToolchain()
                                .getLanguageVersion()
                                .set(JavaLanguageVersion.of(ConventionsProperty.JAVA_VERSION.get(project, DEFAULT_JAVA_VERSION)))
                );

        // Verifiable rebuilds
        tasks.withType(AbstractArchiveTask.class).configureEach(task -> {
            task.setPreserveFileTimestamps(false);
            task.setReproducibleFileOrder(true);
        });
    }

}
