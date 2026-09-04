/*
 * Copyright (c) 2021-2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.conventions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * License Conventions plugin.
 */
public class ConventionsLicensePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        CheckLicenseTask.register(project);
    }

}
