package com.cleanroommc.conventions;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

public enum ConventionsProperty {

    JAVA_VERSION("conventions.javaMajor");

    private final String key;

    ConventionsProperty(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public Object value(Project project) {
        Object value = project.property(key);
        if (value == null) {
            throw new GradleException("Missing property value for " + key);
        }
        return value;
    }

    public String stringValue(Project project) {
        Object value = value(project);
        if (value instanceof String stringValue) {
            return stringValue;
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return key;
    }

}
