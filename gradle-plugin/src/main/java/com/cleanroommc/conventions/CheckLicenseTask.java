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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.work.DisableCachingByDefault;

/**
 * Requires {@code LICENSE} in the project directory or a parent directory to match CleanroomMC License Version 1.0.
 */
@DisableCachingByDefault(because = "License verification is cheap and has no outputs")
public abstract class CheckLicenseTask extends DefaultTask {

    static final String NAME = "checkLicense";

    static void register(Project project) {
        if (project.getTasks().getNames().contains(NAME)) {
            return;
        }
        project.getTasks().register(NAME, CheckLicenseTask.class, task -> {
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            task.setDescription("Requires LICENSE in the project directory or a parent directory to match CleanroomMC License Version 1.0.");
            task.getExpected().convention(ConventionsFile.LICENSE.read());
            task.getStartDirectory().convention(project.getRootProject().getLayout().getProjectDirectory());
        });
        project.getPluginManager()
                .withPlugin("lifecycle-base", _ -> project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(check -> check.dependsOn(NAME)));
    }

    @Input
    public abstract Property<String> getExpected();

    @Internal
    public abstract DirectoryProperty getStartDirectory();

    @TaskAction
    public final void check() throws IOException {
        Path start = getStartDirectory().getAsFile().get().toPath();
        Path license = findLicense(start);
        if (license == null) {
            throw new GradleException(
                    "Missing LICENSE. Run extractConventions, or copy CleanroomMC License Version 1.0 to the project directory or a parent directory."
            );
        }
        String actual = Files.readString(license, StandardCharsets.UTF_8);
        if (!sameLicense(getExpected().get(), actual)) {
            throw new GradleException(license + " does not match CleanroomMC License Version 1.0.");
        }
    }

    static Path findLicense(Path start) {
        Path dir = start.toAbsolutePath().normalize();
        while (dir != null) {
            Path candidate = dir.resolve("LICENSE");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }

    static boolean sameLicense(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    static String normalize(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

}
