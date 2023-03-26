package com.intershop.version.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class CheckIncremation
{
    private final SemanticVersionIncrementor inc = new SemanticVersionIncrementor();

    @Test
    void testMajorMinorPatchReleaseIncrement()
    {
        assertEquals("1.0.0", inc.incrementForRelease(Collections.emptyList(), UpdateStrategy.MINOR));
        assertEquals("2.0.0", inc.incrementForRelease("1.2.3", UpdateStrategy.MAJOR));
        assertEquals("1.3.0", inc.incrementForRelease("1.2.3", UpdateStrategy.MINOR));
        assertEquals("1.2.4", inc.incrementForRelease("1.2.3", UpdateStrategy.PATCH));
    }

    @Test
    void testInvalidStrategies()
    {
        assertThrows(IllegalArgumentException.class, () -> inc.incrementForRelease("1.2.3", UpdateStrategy.STICK));
        assertThrows(IllegalArgumentException.class, () -> inc.incrementForRelease("1.2.3", UpdateStrategy.DEV));
    }

    @Test
    void testListWithReleases()
    {
        assertEquals("1.4.0", inc.incrementForRelease(Arrays.asList("1.2.3", "1.3.1"), UpdateStrategy.MINOR));
        assertEquals("1.4.0", inc.incrementForRelease(Arrays.asList("1.3.1", "1.2.3"), UpdateStrategy.MINOR));
    }

    @Test
    void testListWithPreReleases()
    {
        assertEquals("1.4.0", inc.incrementForRelease(Arrays.asList("1.3.1", "1.4.0-alpha-2"), UpdateStrategy.MINOR));
        // edge case that a pre releases has an higher increment as the strategy -> exception or just warning
        assertEquals("2.0.0", inc.incrementForRelease(Arrays.asList("1.3.1", "2.0.0-alpha-2"), UpdateStrategy.MINOR));
    }

    @Test
    void testListWithPlatformMarker()
    {
        // in case the version (in git) contains the platform marker than the increment should contain it also
        // there could be a problem if different platform marker are in the list, we assume that the latest is the right one
        assertEquals("2.0.0-jre8", inc.incrementForRelease(Arrays.asList("1.3.1-jre8", "2.0.0-alpha-2-jre8"), UpdateStrategy.MINOR));
        assertEquals("2.1.0-jre8", inc.incrementForRelease(Arrays.asList("1.3.1-jre8", "2.0.0-jre8"), UpdateStrategy.MINOR));
    }

    @Test
    void testPreRelease()
    {
        assertEquals("1.0.0-alpha1", inc.incrementForPreRelease(Collections.emptyList(), UpdateStrategy.MINOR));
        // new pre release will be appended
        assertEquals("2.1.0-jre8-alpha1", inc.incrementForPreRelease(Arrays.asList("1.3.1-jre8", "2.0.0-jre8"), UpdateStrategy.MINOR));
        // existing position will be updated - dashes between extension and numbers will be removed, but are semantically equal
        assertEquals("2.0.0-alpha3-jre8", inc.incrementForPreRelease(Arrays.asList("1.3.1-jre8", "2.0.0-alpha-2-jre8"), UpdateStrategy.MINOR));
        assertEquals("2.0.0-alpha3.2-jre8", inc.incrementForPreRelease(Arrays.asList("1.3.1-jre8", "2.0.0-alpha-3.1-jre8"), UpdateStrategy.MINOR));
    }
}
