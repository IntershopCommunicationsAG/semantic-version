package com.intershop.version.semantic;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Test ComparableVersion.
 *
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 */
class CheckMavenIssues {
    
    private final AssertVersion assertVersion = new AssertVersion();

    /**
     * Test <a href="https://issues.apache.org/jira/browse/MNG-5568">MNG-5568</a> edge case
     * which was showing transitive inconsistency: since A &gt; B and B &gt; C then we should have A &gt; C
     * otherwise sorting a list of ComparableVersions() will in some cases throw runtime exception;
     * see Netbeans issues <a href="https://netbeans.org/bugzilla/show_bug.cgi?id=240845">240845</a> and
     * <a href="https://netbeans.org/bugzilla/show_bug.cgi?id=226100">226100</a>
     */
    @Test
    void testMng5568() {
        String a = "6.1.0";
        String b = "6.1.0rc3";
        String c = "6.1H.5-beta"; // this is the unusual version string, with 'H' in the middle

        assertVersion.checkOrder(b, a); // classical
        assertVersion.checkOrder(b, c); // now b < c, but before MNG-5568, we had b > c
        assertVersion.checkOrder(a, c);
    }

    /**
     * Test <a href="https://jira.apache.org/jira/browse/MNG-6572">MNG-6572</a> optimization.
     */
    @Test
    void testMng6572() {
        String a = "20190126.230843"; // resembles a SNAPSHOT
        String b = "1234567890.12345"; // 10 digit number
        String c = "123456789012345.1H.5-beta"; // 15 digit number
        String d = "12345678901234567890.1H.5-beta"; // 20 digit number

        assertVersion.checkOrder(a, b);
        assertVersion.checkOrder(b, c);
        assertVersion.checkOrder(a, c);
        assertVersion.checkOrder(c, d);
        assertVersion.checkOrder(b, d);
        assertVersion.checkOrder(a, d);
    }

    /**
     * Test <a href="https://issues.apache.org/jira/browse/MNG-6964">MNG-6964</a> edge cases
     * for qualifiers that start with "-0.", which was showing A == C and B == C but A &lt; B.
     */
    @Test
    void testMng6964() {
        String a = "1-0.alpha";
        String b = "1-0.beta";
        String c = "1";

        assertVersion.checkOrder(a, c); // Now a < c, but before MNG-6964 they were equal
        assertVersion.checkOrder(b, c); // Now b < c, but before MNG-6964 they were equal
        assertVersion.checkOrder(a, b); // Should still be true
    }

    /**
     * Test <a href="https://issues.apache.org/jira/browse/MNG-7644">MNG-7644</a> edge cases
     * 1.0.0.RC1 &lt; 1.0.0-RC2 and more generally:
     * 1.0.0.X1 &lt; 1.0.0-X2 for any string X
     */
    @Test
    void testMng7644() {
        for (String x : new String[] {"abc", "alpha", "a", "beta", "b", "def", "milestone", "m", "RC"}) {
            // 1.0.0.X1 < 1.0.0-X2 for any string x
            assertVersion.checkOrder("1.0.0." + x + "1", "1.0.0-" + x + "2");
            // 2.0.X == 2-X == 2.0.0.X for any string x
            assertVersion.checkEquals("2-" + x, "2.0." + x); // previously ordered, now equals
            assertVersion.checkEquals("2-" + x, "2.0.0." + x); // previously ordered, now equals
            assertVersion.checkEquals("2.0." + x, "2.0.0." + x); // previously ordered, now equals
        }
    }
    /**
     * Leading zeros are ignored except they are the number
     */
    @Test
    void testLeadingZeroes() {
        assertVersion.checkOrder("0.7", "2");
        assertVersion.checkOrder("0.2", "1.0.7");
    }

    /**
     * Test all versions are equal when starting with many leading zeroes regardless of string length
     * (related to MNG-6572 optimization)
     */
    @Test
    void testVersionEqualWithLeadingZeroes() {
        // versions with string lengths from 1 to 19
        String[] versions = new String[] {
            "0000000000000000001",
            "000000000000000001",
            "00000000000000001",
            "0000000000000001",
            "000000000000001",
            "00000000000001",
            "0000000000001",
            "000000000001",
            "00000000001",
            "0000000001",
            "000000001",
            "00000001",
            "0000001",
            "000001",
            "00001",
            "0001",
            "001",
            "01",
            "1"
        };

        assertVersion.checkEquals(versions);
    }

    /**
     * Test all "0" versions are equal when starting with many leading zeroes regardless of string length
     * (related to MNG-6572 optimization)
     */
    @Test
    void testVersionZeroEqualWithLeadingZeroes() {
        // versions with string lengths from 1 to 19
        String[] versions = new String[] {
            "0000000000000000000",
            "000000000000000000",
            "00000000000000000",
            "0000000000000000",
            "000000000000000",
            "00000000000000",
            "0000000000000",
            "000000000000",
            "00000000000",
            "0000000000",
            "000000000",
            "00000000",
            "0000000",
            "000000",
            "00000",
            "0000",
            "000",
            "00",
            "0"
        };
        assertVersion.checkEquals(versions);
    }

    @Test
    void testCaseInsensitive()
    {
        // case insensitive
        assertVersion.checkEquals("1X", "1x");
        assertVersion.checkEquals("1A", "1a");
        assertVersion.checkEquals("1B", "1b");
        assertVersion.checkEquals("1M", "1m");
        assertVersion.checkEquals("1", "1Ga", "1GA", "1RELEASE", "1release", "1RELeaSE", "1Final", "1FinaL", "1FINAL");
        assertVersion.checkEquals("1Cr", "1Rc", "1cR", "1rC");
        assertVersion.checkEquals("1m3", "1Milestone3", "1MileStone3", "1MILESTONE3");
    }

    @Test
    void testLocaleIndependent()
    {
        Locale orig = Locale.getDefault();
        Locale[] locales = { Locale.ENGLISH, new Locale("tr"), Locale.getDefault() };
        try
        {
            for (Locale locale : locales)
            {
                Locale.setDefault(locale);
                assertVersion.checkEquals("1-abcdefghijklmnopqrstuvwxyz", "1-ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            }
        }
        finally
        {
            Locale.setDefault(orig);
        }
    }
}
