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

class ExtractConventionsTaskTest {

    @Test
    void mergeReplacesTheMarkedRegionAndKeepsTheRest() {
        String existing = "# >>> cleanroom-conventions\nold/\n# <<< cleanroom-conventions\nmine.iml\n";
        String incoming = "# >>> cleanroom-conventions\nnew/\n# <<< cleanroom-conventions\n";
        assertThat(ExtractConventionsTask.mergeMarkedRegion(existing, incoming)).isEqualTo(
                "# >>> cleanroom-conventions\nnew/\n# <<< cleanroom-conventions\nmine.iml\n"
        );
    }

    @Test
    void mergePrependsWhenTheExistingFileHasNoMarkers() {
        String existing = "mine.iml\n";
        String incoming = "# >>> cleanroom-conventions\nnew/\n# <<< cleanroom-conventions\n";
        assertThat(ExtractConventionsTask.mergeMarkedRegion(existing, incoming)).isEqualTo(
                "# >>> cleanroom-conventions\nnew/\n# <<< cleanroom-conventions\nmine.iml\n"
        );
    }

}
