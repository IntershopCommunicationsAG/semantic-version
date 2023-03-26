package com.intershop.version.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class VersionItemTest
{
    @Test
    void testAddExtensionNumer()
    {
        VersionItem version = VersionItem.emptyVersion()
                                         .addNumber(1)
                                         .addNumber(2)
                                         .addNumber(3)
                                         .addExtension(ExtensionType.PLATFORM, "jre")
                                         .addExtensionNumber("11");
        assertEquals("jre11", version.getExtension());
        List<VersionExtensionItem> extensions = version.getExtensions();
        assertEquals(1, extensions.size());
        assertEquals("jre", extensions.get(0).getExtension());
        assertEquals(1, extensions.get(0).getNumbers().size());
        assertEquals(VersionNumberItems.parseItem("11"), extensions.get(0).getNumbers().get(0));
        assertEquals("jre11", version.getExtension());
    }
}
