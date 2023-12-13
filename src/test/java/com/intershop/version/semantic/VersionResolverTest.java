package com.intershop.version.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class VersionResolverTest
{
    private final SemanticVersionResolverImpl resolver = new SemanticVersionResolverImpl();

    @Test
    void testSplitVersion()
    {
        assertEquals(Arrays.asList("1", "0", "alpha", "1"), resolver.splitVersion("1.0-alpha-1"));
        assertEquals(Arrays.asList("1", "0", "alpha", "1"), resolver.splitVersion("1.0-alpha1"));
        assertEquals(Arrays.asList("1", "0", "alpha", "1", "a"), resolver.splitVersion("1.0-alpha1a"));
    }

    @Test
    void testAlphaVersion()
    {
        VersionItem item = resolver.convertToItem(Arrays.asList("1", "0"), Arrays.asList("alpha", "1"));
        assertEquals(2, item.getNumbers().size());
        assertEquals(1, item.getNumbers().get(0));
        assertEquals(0, item.getNumbers().get(1));
        assertEquals(ExtensionType.DEV, item.getReleaseType());
        assertEquals(1, item.getExtensions().size());
        assertEquals("alpha", item.getExtensions().get(0).getExtension());
    }

    @Test
    void testOnlyNumbers()
    {
        VersionItem item = resolver.convertToItem(resolver.splitVersion("1.0"), Collections.emptyList());
        assertEquals(2, item.getNumbers().size());
        assertEquals(1, item.getNumbers().get(0));
        assertEquals(0, item.getNumbers().get(1));
        assertEquals(ExtensionType.GA, item.getReleaseType());
        assertEquals(0, item.getExtensions().size());
    }

    @Test
    void testPreReleaseOfAlphaVersion()
    {
        VersionItem item = resolver.convertToItem(Arrays.asList("1", "0"), Arrays.asList("alpha", "1", "snapshot"));
        assertEquals(2, item.getNumbers().size());
        assertEquals(1, item.getNumbers().get(0));
        assertEquals(0, item.getNumbers().get(1));
        assertEquals(ExtensionType.DEV, item.getReleaseType());
        assertEquals(2, item.getExtensions().size());
        assertEquals("alpha", item.getExtensions().get(0).getExtension());
        assertEquals(1, item.getExtensions().get(0).getNumbers().size());
        assertEquals(VersionNumberItems.parseItem("1"), item.getExtensions().get(0).getNumbers().get(0));
        assertEquals("snapshot", item.getExtensions().get(1).getExtension());
        assertEquals(ExtensionType.DEV, item.getExtensions().get(1).getExtensionType());
    }
    
    @Test
    void testJetty()
    {
        VersionItem item = resolver.convertToItem(Arrays.asList("1", "2", "3"), Arrays.asList("v", "20201212"));
        assertEquals(3, item.getNumbers().size());
        assertEquals(1, item.getNumbers().get(0));
        assertEquals(2, item.getNumbers().get(1));
        assertEquals(3, item.getNumbers().get(2));
        assertEquals(ExtensionType.GA, item.getReleaseType());
        assertEquals(1, item.getExtensions().size());
        assertEquals("", item.getExtensions().get(0).getExtension());
        assertEquals(ExtensionType.BUILD, item.getExtensions().get(0).getExtensionType());
        assertEquals(VersionNumberItems.parseItem("20201212"), item.getExtensions().get(0).getNumbers().get(0));
    }

    @Test
    void testPlatformExtension()
    {
        VersionItem item = resolver.convertToItem(Arrays.asList("1", "2", "3", "jre", "8"), Collections.emptyList());
        assertEquals(3, item.getNumbers().size());
        assertEquals(1, item.getNumbers().get(0));
        assertEquals(2, item.getNumbers().get(1));
        assertEquals(ExtensionType.GA, item.getReleaseType());
        assertEquals(1, item.getExtensions().size());
        assertEquals(ExtensionType.PLATFORM, item.getExtensions().get(0).getExtensionType());
    }

    @Test
    void testVeryLongNumber()
    {
        VersionItem item = resolver.convertToItem(Arrays.asList("1", "2", "3", "v", "123456789012345678901234567890"), Collections.emptyList());
        assertEquals(1, item.getExtensions().size());
    }

    @Test
    void testBuildExtension()
    {
        VersionItem item = resolver.convertToItem(Arrays.asList("1", "2", "3", "+", "20230101"), Collections.emptyList());
        assertEquals(1, item.getExtensions().size());
        assertEquals(ExtensionType.BUILD, item.getExtensions().get(0).getExtensionType());
    }
}
