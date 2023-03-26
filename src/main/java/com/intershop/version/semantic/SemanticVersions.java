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

/**
 * Provides migration strategies for libraries
 */
public class SemanticVersions
{
    private SemanticVersions() {}

    private static final SemanticVersionMigration SEMANTIC_VERSION_MIGRATION = new SemanticVersionMigration();

    /**
     * Find release, with given update strategy.
     * 
     * @param allowedChanges update strategy
     * @param versions available version
     * @param current version
     * @return the newest version or Optional.empty()
     * @deprecated use {@link SemanticVersionMigration#getMigration(UpdateStrategy, Collection, String)}
     */
    @Deprecated
    public static String getNewestVersion(UpdateStrategy allowedChanges, Collection<String> versions, String current)
    {
        return SEMANTIC_VERSION_MIGRATION.getMigration(allowedChanges, versions, current).orElse(current);
    }

    /**
     * Verify that a new version can be reached with a given update strategy
     * @param oldVersion Old version
     * @param newVersion New version
     * @param strategy Update strategy
     * @return true in case the new version is reachable (e.g. MINOR update requested 1.2.3 a minor update of 1.1.1)
     * @deprecated use {@link SemanticVersionMigration#isMigrationStepPossible(SemanticVersion, SemanticVersion, UpdateStrategy)}
     */
    @Deprecated
    public static boolean isMigrationStepPossible(SemanticVersion oldVersion, SemanticVersion newVersion, UpdateStrategy strategy)
    {
        return SEMANTIC_VERSION_MIGRATION.isMigrationStepPossible(oldVersion, newVersion, strategy);
    }
}
