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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

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
    void checkstyleHeaderMatchesTheHeaderFile() {
        Matcher matcher = Pattern.compile("<module name=\"Header\">\\s*<property name=\"header\" value=\"([^\"]*)\"/>", Pattern.DOTALL)
                .matcher(ConventionsFile.CHECKSTYLE.read());
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1).replace("\\n", "\n")).isEqualTo(ConventionsFile.javaHeader());
    }

}
