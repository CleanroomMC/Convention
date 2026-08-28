package com.cleanroommc.conventions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public enum ConventionsFile {

    FORMAT_J("formatj.toml"),
    CHECKSTYLE("checkstyle.xml"),
    CLIFF("cliff.toml");

    private static final String RESOURCE_DIRECTORY = "/resources/";

    private final String fileName;

    ConventionsFile(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }

    /**
     * Extracts file from plugin jar into the repo's directory for tools to read.
     */
    File unpack(Project project) {
        Path target = project.getLayout().getProjectDirectory().file(fileName).getAsFile().toPath();
        try (InputStream stream = ConventionsFile.class.getResourceAsStream(RESOURCE_DIRECTORY + fileName)) {
            if (stream == null) {
                throw new GradleException("Convention file " + fileName + " is missing from the conventions plugin jar");
            }
            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return target.toFile();
    }

    @Override
    public String toString() {
        return fileName;
    }

}
