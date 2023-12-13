/*
 * Copyright 2020 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.intershop.version.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SemanticVersionTest
{
    private final AssertVersion assertVersion = new AssertVersion();
    @Test
    void testMajorMinorPatch()
    {
        SemanticVersion version_1_2_3 = SemanticVersion.valueOf("1.2.3");
        assertEquals(1, version_1_2_3.getMajor(), "major");
        assertEquals(2, version_1_2_3.getMinor(), "minor");
        assertEquals(3, version_1_2_3.getPatch(), "patch");
    }

    @Test
    void testMajorMinor()
    {
        SemanticVersion version_16_3 = SemanticVersion.valueOf("16.3");
        assertEquals(16, version_16_3.getMajor(), "major");
        assertEquals(3, version_16_3.getMinor(), "minor");
        assertEquals(0, version_16_3.getPatch(), "patch");
    }

    @Test
    void testIncrementVersions()
    {
        assertVersion.checkOrder("2.7.6-dev1", "2.7.6-rc1");
    }

    @Test
    void testNonSemanticVersions()
    {
        assertVersion.checkOrder("23423842", "23423842.3242");
    }

    @Test
    void testFeatureBranchVersions()
    {
        assertVersion.checkOrder("FB-12.0.0-ANYISUSE-12233", "FB-13.0.0-ANYISUSE-12233");
    }

    @Test
    void testShortSemanticVersions()
    {
        assertVersion.checkOrder("2", "2.7");
    }

    @Test
    void testExtendedSemanticDashVersions()
    {
        assertVersion.checkEquals("2.7.6.GA", "2.7.6.FINAL", "2.7.6-GA", "2.7.6-FINAL");
    }

    @Test
    void testFourDigitsAndMore()
    {
        assertVersion.checkOrder("2.7.6.0", "2.7.6.1", "5.4.3.2.1", "6.5.4.3.2.1");
    }

    @Test
    void testJettyVersion()
    {
        assertEquals(14, SemanticVersion.valueOf("9.3.14.v20161028").getPatch(), "find patch in jetty version");
    }

    @Test
    void testGetVersionWithoutBuildExtension()
    {
        assertEquals("11.8.0", SemanticVersion.valueOf("11.8.0-SNAPSHOT").getVersionWithoutBuildExtension());
        assertEquals("11.8.0", SemanticVersion.valueOf("11.8.0-LOCAL").getVersionWithoutBuildExtension());
        assertEquals("11.8.0", SemanticVersion.valueOf("11.8.0-dev1").getVersionWithoutBuildExtension());
        assertEquals("7.8.0.1", SemanticVersion.valueOf("7.8.0.1").getVersionWithoutBuildExtension());
        assertEquals("17.0.0", SemanticVersion.valueOf("17.0").getVersionWithoutBuildExtension());
        assertEquals("14.0.0", SemanticVersion.valueOf("14.0.0-IS_12345_check_feature_test_master")
                        .getVersionWithoutBuildExtension());
    }

    @Test
    void testShortSemanticVersionEquals()
    {
        assertVersion.checkEquals("2.5", "2.5");
        assertTrue(SemanticVersion.valueOf("2.5").equals(SemanticVersion.valueOf("2.5")));
        assertEquals(SemanticVersion.valueOf("2.5").hashCode(), SemanticVersion.valueOf("2.5").hashCode());
    }
}
