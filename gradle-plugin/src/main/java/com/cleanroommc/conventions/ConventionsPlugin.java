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
        project.getPluginManager().apply(ConventionsTestingPlugin.class);
        project.getPluginManager().apply(ConventionsPublishingPlugin.class);
        if (ConventionsProperty.MOD_PUBLISHING.map(project).map(Boolean::parseBoolean).getOrElse(false)) {
            project.getPluginManager().apply(ConventionsModPlugin.class);
        }
    }

}
