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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.gradle.api.GradleException;

enum ConventionsFile {

    FORMAT_J("formatj.toml"),
    CHECKSTYLE("checkstyle.xml"),
    CLIFF("cliff.toml"),
    LICENSE("LICENSE"),
    HEADER("HEADER"),
    EDITOR_CONFIG(".editorconfig", "editorconfig"),
    GIT_ATTRIBUTES(".gitattributes", "gitattributes"),
    GIT_IGNORE(".gitignore", "gitignore", true);

    private static final String RESOURCE_DIRECTORY = "/resources/";

    private final String fileName;
    private final String resourceName;
    private final boolean mergeMarkedRegion;

    ConventionsFile(String fileName) {
        this(fileName, fileName, false);
    }

    ConventionsFile(String fileName, String resourceName) {
        this(fileName, resourceName, false);
    }

    ConventionsFile(String fileName, String resourceName, boolean mergeMarkedRegion) {
        this.fileName = fileName;
        this.resourceName = resourceName;
        this.mergeMarkedRegion = mergeMarkedRegion;
    }

    String fileName() {
        return fileName;
    }

    boolean mergeMarkedRegion() {
        return mergeMarkedRegion;
    }

    static String javaHeader() {
        return toJavaBlockComment(HEADER.read());
    }

    static String toJavaBlockComment(String text) {
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        while (normalized.endsWith("\n")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        StringBuilder comment = new StringBuilder("/*\n");
        if (!normalized.isEmpty()) {
            for (String line : normalized.split("\n", -1)) {
                if (line.isEmpty()) {
                    comment.append(" *\n");
                } else {
                    comment.append(" * ").append(line).append('\n');
                }
            }
        }
        return comment.append(" */").toString();
    }

    String read() {
        try (InputStream stream = ConventionsFile.class.getResourceAsStream(RESOURCE_DIRECTORY + resourceName)) {
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
