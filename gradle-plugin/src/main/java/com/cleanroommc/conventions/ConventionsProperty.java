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

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

enum ConventionsProperty {

    JAVA_VERSION("conventions.javaMajor"),
    MOD_PUBLISHING("conventions.modPublishing"),
    REPO_URL("conventions.repoUrl"),
    CHECKSTYLE_VERSION("conventions.checkstyleVersion"),
    JUNIT_VERSION("conventions.junitVersion"),
    MOCKITO_VERSION("conventions.mockitoVersion"),
    ASSERTJ_VERSION("conventions.assertjVersion");

    private final String key;

    ConventionsProperty(String key) {
        this.key = key;
    }

    Provider<String> provider(Project project) {
        return project.getProviders().gradleProperty(key);
    }

    String get(Project project, String defaultValue) {
        return provider(project).getOrElse(defaultValue);
    }

    @Override
    public String toString() {
        return key;
    }

}
