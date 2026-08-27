package com.cleanroommc.conventions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The default overlay for a new CleanroomMC library, mod or tool.
 */
public class ConventionsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ConventionsBasePlugin.class);
        project.getPluginManager().apply(ConventionsStylePlugin.class);
    }

}
