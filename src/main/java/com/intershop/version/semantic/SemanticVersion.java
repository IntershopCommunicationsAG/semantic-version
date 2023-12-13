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

import java.util.Optional;

/**
 * public interface to represent a semantic version
 */
public interface SemanticVersion extends Comparable<SemanticVersion>
{
    /**
     * @param version version as string
     * @return semantic version to retrieve semantic meaning
     */
    static SemanticVersion valueOf(String version)
    {
        return new SemanticVersionResolverImpl().apply(version);
    }

    /**
     * @return the major version component
     */
    int getMajor();

    /**
     * @return the minor version component
     */
    int getMinor();

    /**
     * @return the patch version component
     */
    int getPatch();

    /**
     * @return the version string that had been used to create this instance
     */
    String getVersion();

    /**
     * @return true if the given version is recommended for production environments
     */
    default boolean isRecommendedForProduction()
    {
        return getReleaseType().isRecommendedForProduction();
    }

    /**
     * @return the increment state (release type) of this version
     */
    ExtensionType getReleaseType();

    /**
     * @return the text behind the semantic version
     */
    String getExtension();

    /**
     * @return the platform extensions with a leading dash or Optional.empty();
     */
    Optional<String> getPlatformExtension();

    /**
     * @return semantic version without any extensions so major.minor.patch and additional numbers for versions with
     *         more than 3 numbers.
     */
    default String getVersionWithoutBuildExtension()
    {
        return new StringBuilder().append(getMajor())
                                  .append(".")
                                  .append(getMinor())
                                  .append(".")
                                  .append(getPatch())
                                  .toString();
    }

    /**
     * @return the semantic version, which would be the next following of the current (e.g. 1.0.0-alpha-2 for 1.0.0-alpha-1)
     */
    SemanticVersion incrementLastIncrement();
}
