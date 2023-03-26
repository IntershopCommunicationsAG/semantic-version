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

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Compares and filters given versions with the current version
 */
public class SemanticVersionMigration
{
    private final SemanticVersionResolver resolver;
    private final Comparator<SemanticVersion> comparator;

    /**
     * Constructor with predefined version resolver and comparator
     */
    public SemanticVersionMigration()
    {
        this(new VersionResolver(), VersionComparators.VERSION_COMPARATOR);
    }

    /**
     * Constructor with extenal defined resolver and comparator
     * 
     * @param resolver converts a string to a semantic version
     * @param comparator can sort versions
     */
    public SemanticVersionMigration(SemanticVersionResolver resolver, Comparator<SemanticVersion> comparator)
    {
        this.resolver = resolver;
        this.comparator = comparator;
    }

    private SemanticVersionResolver getResolver()
    {
        return resolver;
    }

    SemanticVersion valueOf(String version)
    {
        return getResolver().apply(version);
    }

    /**
     * Find release, with given update strategy.
     * 
     * @param allowedChanges update strategy
     * @param versions available version
     * @param current version
     * @return the newest version or Optional.empty()
     */
    public Optional<String> getMigration(UpdateStrategy allowedChanges, Collection<String> versions, String current)
    {
        return getMigration(allowedChanges, versions.stream().map(this::valueOf).collect(Collectors.toList()),
                        valueOf(current)).map(SemanticVersion::getVersion);
    }

    private Optional<SemanticVersion> getMigration(UpdateStrategy allowedChanges, Collection<SemanticVersion> versions,
                    SemanticVersion current)
    {
        Optional<SemanticVersion> result;
        switch(allowedChanges)
        {
            case MAJOR:
                result = getNewestMajorVersion(versions);
                break;
            case MINOR:
                result = getNewestMinorVersion(versions, current);
                break;
            case PATCH:
                result = getNewestPatchVersion(versions, current);
                break;
            case DEV:
                result = getNewestAvailableVersion(versions);
                break;
            case INC:
                result = getNewestAvailableVersion(versions);
                break;
            case STICK:
                result = Optional.empty();
                break;
            default:
                throw new IllegalArgumentException("Unknown meaning provided");
        }
        if (result.isPresent() && result.get().equals(current))
        {
            result = Optional.empty();
        }
        return result;
    }

    /**
     * Find release, where only the patch version is updated. (used for stabilization branches)
     * 
     * @param versions available version
     * @param current version
     * @return the newest patch version or if no version could be determined the current
     */
    private Optional<SemanticVersion> getNewestPatchVersion(Collection<SemanticVersion> versions,
                    SemanticVersion current)
    {
        return versions.stream()
                       .filter(v -> v.getMajor() == current.getMajor())
                       .filter(v -> v.getMinor() == current.getMinor())
                       .filter(SemanticVersion::isRecommendedForProduction)
                       .max(comparator);
    }

    /**
     * Find release, where only the patch version is updated. (used for stabilization branches)
     *
     * @param current version
     * @param versions available version
     * @return the newest minor version or if no version could be determined the current
     */
    private Optional<SemanticVersion> getNewestMinorVersion(Collection<SemanticVersion> versions,
                    SemanticVersion current)
    {
        return versions.stream()
                       .filter(v -> v.getMajor() == current.getMajor())
                       .filter(SemanticVersion::isRecommendedForProduction)
                       .max(comparator);
    }

    /**
     * Find release, where major,minor,patch version can be updated. (used external dependencies for trunk/master/main)
     *
     * @param versions available version
     * @return the newest major version or if no version could be determined the current
     */
    private Optional<SemanticVersion> getNewestMajorVersion(Collection<SemanticVersion> versions)
    {
        return versions.stream().filter(SemanticVersion::isRecommendedForProduction).max(comparator);
    }

    /**
     * Find any increment, where major,minor,patch,increment can be updated. (used internal dependencies for
     * trunk/master)
     *
     * @param versions available version
     * @return the newest major version or if no version could be determined the current, also non production versions
     *         can be selected.
     */
    private Optional<SemanticVersion> getNewestAvailableVersion(Collection<SemanticVersion> versions)
    {
        return versions.stream().max(comparator);
    }

    /**
     * Verify that a new version can be reached with a given update strategy. The function doesn't check that the new
     * version is really newer. The function doesn't check that the version is "production" ready.
     * 
     * @param oldVersion Old version
     * @param newVersion New version
     * @param strategy Update strategy
     * @return true in case the new version is reachable (e.g. MINOR update requested 1.2.3 a minor update of 1.1.1)
     */
    public boolean isMigrationStepPossible(SemanticVersion oldVersion, SemanticVersion newVersion,
                    UpdateStrategy strategy)
    {
        // if the strategy is major than all new versions are allowed
        if (UpdateStrategy.MAJOR == strategy)
        {
            return true;
        }
        // if the strategy is less major than major updates are not allowed
        if (oldVersion.getMajor() != newVersion.getMajor())
        {
            return false;
        }
        if (UpdateStrategy.MINOR.equals(strategy))
        {
            return true;
        }
        if (oldVersion.getMinor() != newVersion.getMinor())
        {
            return false;
        }
        if (UpdateStrategy.PATCH.equals(strategy))
        {
            return true;
        }
        if (oldVersion.getPatch() != newVersion.getPatch())
        {
            return false;
        }
        return true;
    }
}
