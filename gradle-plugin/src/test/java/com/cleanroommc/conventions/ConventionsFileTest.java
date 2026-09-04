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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ConventionsFileTest {

    @Test
    void javaHeaderWrapsTheHeaderFile() {
        assertThat(ConventionsFile.javaHeader()).isEqualTo(
                """
                /*
                 * Copyright (c) 2021-present CleanroomMC contributors
                 *
                 * This file is licensed under the CleanroomMC License Version 1.0.
                 * See the applicable LICENSE file in this directory or a parent directory
                 * for the full licence terms.
                 *
                 * This is visible-source software and is not open-source software.
                 */"""
        );
    }

    @Test
    void checkstyleContainsTheLicenseHeaderPlaceholder() {
        assertThat(ConventionsFile.CHECKSTYLE.read()).contains("<property name=\"header\" value=\"@LICENSE_HEADER@\"/>");
    }

    @ParameterizedTest
    @EnumSource(LicenseMode.class)
    void checkstyleRendersEveryLicenseHeader(LicenseMode license) {
        assertThat(ConventionsFile.checkstyle(license)).doesNotContain("@LICENSE_HEADER@");
        assertThat(ConventionsFile.checkstyle(license)).contains(license.headerText().lines().findFirst().orElseThrow());
    }

}
