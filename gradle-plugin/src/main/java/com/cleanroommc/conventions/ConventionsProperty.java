package com.cleanroommc.conventions;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public enum ConventionsProperty {

    JAVA_VERSION("conventions.javaMajor"),
    MOD_PUBLISHING("conventions.modPublishing");

    private final String key;

    ConventionsProperty(String key) {
        this.key = key;
    }

    public Provider<String> map(Project project) {
        return project.getProviders().gradleProperty(key);
    }

    public String get(Project project, String defaultValue) {
        return map(project).getOrElse(defaultValue);
    }

    public String get(Project project) {
        return map(project).get();
    }

    @Override
    public String toString() {
        return key;
    }

}
