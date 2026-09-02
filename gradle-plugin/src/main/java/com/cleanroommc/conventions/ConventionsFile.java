package com.cleanroommc.conventions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.gradle.api.GradleException;

enum ConventionsFile {

    FORMAT_J("formatj.toml"),
    CHECKSTYLE("checkstyle.xml"),
    CLIFF("cliff.toml");

    private static final String RESOURCE_DIRECTORY = "/resources/";

    private final String fileName;

    ConventionsFile(String fileName) {
        this.fileName = fileName;
    }

    String read() {
        try (InputStream stream = ConventionsFile.class.getResourceAsStream(RESOURCE_DIRECTORY + fileName)) {
            if (stream == null) {
                throw new GradleException("Convention file " + fileName + " is missing from the conventions plugin jar");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read convention file " + fileName + " from the conventions plugin jar", e);
        }
    }

    @Override
    public String toString() {
        return fileName;
    }

}
