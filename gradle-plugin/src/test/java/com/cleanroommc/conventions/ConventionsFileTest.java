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

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ConventionsFileTest {

    @Test
    void javaHeaderWrapsTheHeaderFile() {
        int current = Year.now().getValue();
        assertThat(LicenseMode.VISIBLE.javaHeader(LicenseYears.current())).isEqualTo(
                """
                /*
                 * Copyright (c) %d CleanroomMC contributors
                 *
                 * This file is licensed under the CleanroomMC License Version 1.0.
                 * See the applicable LICENSE file in this directory or a parent directory
                 * for the full licence terms.
                 *
                 * This is visible-source software and is not open-source software.
                 */""".formatted(
                        current
                )
        );
    }

    @Test
    void checkstyleContainsTheLicenseHeaderPlaceholder() {
        assertThat(ConventionsFile.CHECKSTYLE.read()).contains("<property name=\"header\" value=\"@LICENSE_HEADER@\"/>");
    }

    @ParameterizedTest
    @EnumSource(LicenseMode.class)
    void checkstyleRendersEveryLicenseHeader(LicenseMode license) {
        LicenseYears years = LicenseYears.of(2021, 2026);
        assertThat(ConventionsFile.checkstyle(license, years)).doesNotContain("@LICENSE_HEADER@");
        assertThat(ConventionsFile.checkstyle(license, years)).contains(license.headerText(years).lines().findFirst().orElseThrow());
    }

}
