package com.intershop.version.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;

/**
 * Assertions for compare versions.
 * 
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 */
public class AssertVersion {
    
    private final Comparator<String> comparator;

    public AssertVersion()
    {
        this(VersionComparators.STRING_COMPARATOR);
    }

    public AssertVersion(Comparator<String> comparator)
    {
        this.comparator = comparator;
    }
    
    private int compareTo(String v1, String v2)
    {
        return comparator.compare(v1, v2);
    }

    private void checkOrderOfTwo(String v1, String v2) {
        assertTrue(compareTo(v1, v2) < 0, "expected " + v1 + " < " + v2);
        assertTrue(compareTo(v2, v1) > 0, "expected " + v2 + " > " + v1);
    }

    public void checkOrder(String... versions) {
        for (int i = 1; i < versions.length; i++) {
            for (int j = i; j < versions.length; j++) {
                checkOrderOfTwo(versions[i - 1], versions[j]);
            }
        }
    }

    private void checkEqualVersions(String v1, String v2) {
        assertEquals(0, compareTo(v1,v2), "expected " + v1 + " == " + v2);
        assertEquals(0, compareTo(v2,v1), "expected " + v2 + " == " + v1);
    }

    /**
     * Checks that all provided versions are semantically equal (in both directions v1.equal(v2) v2.equal(v1)
     * @param versions
     */
    public void checkEquals(String... versions) {
        for (int i = 0; i < versions.length; ++i) {
            for (int j = i + 1; j < versions.length; ++j) {
                checkEqualVersions(versions[i], versions[j]);
            }
            // compare against each other (including itself) but later
            checkEqualVersions(versions[i], versions[i]);
        }
    }
}
