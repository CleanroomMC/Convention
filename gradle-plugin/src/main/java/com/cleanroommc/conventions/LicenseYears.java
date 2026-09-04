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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

final class LicenseYears {

    private static final String YEAR_TOKEN = "@YEAR@";
    private static final Pattern COPYRIGHT = Pattern.compile("(?m)^(Copyright (?:\\(c\\)|©) )(\\d{4})(?:-(?:\\d{4}|present))?( CleanroomMC contributors)$");

    private final int begin;
    private final int current;

    private LicenseYears(int begin, int current) {
        if (begin < 1000) {
            throw new GradleException("conventions.beginFrom must be a four-digit year.");
        }
        if (begin > current) {
            throw new GradleException("conventions.beginFrom must not be later than the current year " + current + ".");
        }
        this.begin = begin;
        this.current = current;
    }

    static Provider<LicenseYears> provider(Project project) {
        ConventionsExtension conventions = ConventionsExtension.register(project);
        Provider<Integer> current = project.provider(() -> Year.now().getValue());
        return current.zip(conventions.getBeginFrom(), (currentYear, begin) -> new LicenseYears(begin, currentYear));
    }

    static LicenseYears of(int begin, int current) {
        return new LicenseYears(begin, current);
    }

    static LicenseYears current() {
        int current = Year.now().getValue();
        return new LicenseYears(current, current);
    }

    String value() {
        return begin == current ? Integer.toString(current) : begin + "-" + current;
    }

    String apply(String text) {
        return text.replace(YEAR_TOKEN, value());
    }

    static int persistedBegin(Path directory, int current) {
        Path cursor = directory;
        while (cursor != null) {
            for (String fileName : new String[] { "HEADER", "LICENSE" }) {
                Path file = cursor.resolve(fileName);
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                try {
                    Matcher matcher = COPYRIGHT.matcher(Files.readString(file));
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(2));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Cannot read persisted copyright year from " + file, e);
                }
            }
            cursor = cursor.getParent();
        }
        return current;
    }

}
