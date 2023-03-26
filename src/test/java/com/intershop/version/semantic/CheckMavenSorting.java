package com.intershop.version.semantic;

import org.junit.jupiter.api.Test;

/**
 * Test ComparableVersion.
 *
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 */
class CheckMavenSorting
{
    private final AssertVersion assertVersion = new AssertVersion();

    @Test
    void checkSemanticEqualVersions()
    {
        assertVersion.checkEquals("1", "1.0", "1.0.0", "1-0", "1.0-0");
        // no separator between number and character
        assertVersion.checkEquals("1a", "1-a", "1.0a", "1.0-a", "1.0.0-a", "1.0.0a");
        assertVersion.checkEquals("1x", "1-x", "1.0x", "1.0-x", "1.0.0-x", "1.0.0x");
    }

    @Test
    void checkAliases()
    {
        // aliases for GAs
        assertVersion.checkEquals("1", "1ga", "1release", "1final", "1.0.0-GA", "1.0.0.FINAL");
        // candidate release == release candidate
        assertVersion.checkEquals("1cr", "1rc");

        // special "aliases" a, b and m for alpha, beta and milestone
        assertVersion.checkEquals("1a1", "1-alpha-1", "1.0.0-alpha-1.0");
        assertVersion.checkEquals("1b2", "1-beta-2");
        assertVersion.checkEquals("1m3", "1-milestone-3");
    }

    @Test
    void checkPlatformIndependency()
    {
        // jre platform extension
        assertVersion.checkEquals("1.0.0-jre8", "1.0.0-jre17");
    }

    @Test
    void testBasic3Digits()
    {
        assertVersion.checkOrder("1", "1.0.1", "1.1", "1.2", "1.5", "1.12", "2", "2.5");
        assertVersion.checkOrder("1.0.0", "1.1", "1.2.0");
    }

    @Test
    void testWellDefinedExtension()
    {
        // check sort of alpha and beta releases
        assertVersion.checkOrder("1.0-alpha-1", "1.0-alpha-2", "1.0-beta-1", "1.0");
        // check sort of alpha and beta releases with snapshots
        assertVersion.checkOrder("1.0-alpha-1-SNAPSHOT", "1.0-alpha-1", "1.0-beta-1", "1.0-SNAPSHOT", "1.0");
        // check one vs more numbers
        assertVersion.checkOrder("1.2.3-alpha-1.0" , "1.2.3-alpha-1.1");
        // check sort with dash separator
        assertVersion.checkOrder("1.0", "1.0-1", "1.0-2");
        assertVersion.checkOrder("1.0.0", "1.0-1", "2.0-1", "2.0.1", "2.0.1-123");
        // check different length
        assertVersion.checkOrder("1.0-alpha-1-dev1", "1.0-alpha-1");
        // check different length
        assertVersion.checkOrder("1.0-alpha-1", "1.0-alpha-1.2");
    }

    @Test
    void testKnownExtension()
    {
        // alphabetically
        assertVersion.checkOrder("2.0.1-klm", "2.0.1-lmn");
        // GA is less as with extension
        assertVersion.checkOrder("2.0.1", "2.0.1-xyz");
        // numbers are greater than unknown words or letter combinations
        assertVersion.checkOrder("2.0.1-xyz", "2.0.1-123");
    }

    @Test
    void testVeryLongNumbers()
    {
        // int vs int
        assertVersion.checkOrder("1.2.3-alpha-12345678" , "1.2.3-alpha-12345679");
        // long vs long
        assertVersion.checkOrder("1.2.3-alpha-922337203685477580" , "1.2.3-alpha-922337203685477581");
        // big int vs big int
        assertVersion.checkOrder("1.2.3-alpha-123456789012345678901234567890", "1.2.3-alpha-123456789012345678901234567891");
        // int vs long vs big int (make sure that not string compare is used)
        assertVersion.checkOrder("1.2.3-alpha-12345679" , "1.2.3-alpha-922337203685477580", "1.2.3-alpha-113456789012345678901234567890");
    }

    @Test
    void testBuildExtension()
    {
        // build versions are semantically equal, but a newer build should be preferred
        assertVersion.checkOrder("1.2.3-alpha-1+20230201", "1.2.3+20230101", "1.2.3+20230102", "1.2.4-alpha-1+20230301");
    }

    @Test
    void testPlatformNonRelevance()
    {
        // sorting ignores platform
        assertVersion.checkOrder("1.2.3-jre8-alpha-1", "1.2.3-alpha-2-jre8");
    }

    @Test
    void testNumberExtensionsVsNoNumber()
    {
        assertVersion.checkOrder("1", "1-0.1");
        assertVersion.checkOrder("1-alpha", "1-alpha-0.1");
    }

}
