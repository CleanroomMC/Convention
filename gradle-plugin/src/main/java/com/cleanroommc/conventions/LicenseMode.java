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

import java.util.Locale;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

enum LicenseMode {

    FREE("free", "licenses/free/LICENSE", "licenses/free/HEADER", "MIT License", "https://opensource.org/license/mit/", ""),
    OPEN("open", "licenses/open/LICENSE", "licenses/open/HEADER", "GNU Lesser General Public License v3.0", "https://www.gnu.org/licenses/lgpl-3.0.html", ""),
    VISIBLE("visible", "LICENSE", "HEADER", ConventionsDefaults.LICENSE_NAME, ConventionsDefaults.LICENSE_URL, ConventionsDefaults.LICENSE_COMMENTS);

    private final String propertyValue;
    private final String licenseResource;
    private final String headerResource;
    private final String displayName;
    private final String url;
    private final String comments;

    LicenseMode(String propertyValue, String licenseResource, String headerResource, String displayName, String url, String comments) {
        this.propertyValue = propertyValue;
        this.licenseResource = licenseResource;
        this.headerResource = headerResource;
        this.displayName = displayName;
        this.url = url;
        this.comments = comments;
    }

    static LicenseMode from(Project project) {
        return from(ConventionsProperty.LICENSE.provider(project).getOrElse(VISIBLE.propertyValue));
    }

    static LicenseMode from(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (LicenseMode mode : values()) {
            if (mode.propertyValue.equals(normalized)) {
                return mode;
            }
        }
        throw new GradleException("Unknown license mode '" + value + "'. Expected free, open or visible.");
    }

    String propertyValue() {
        return propertyValue;
    }

    String licenseText() {
        return ConventionsFile.readResource(licenseResource);
    }

    String headerText() {
        return ConventionsFile.readResource(headerResource);
    }

    String javaHeader() {
        return ConventionsFile.toJavaBlockComment(headerText());
    }

    String displayName() {
        return displayName;
    }

    String url() {
        return url;
    }

    String comments() {
        return comments;
    }

}
