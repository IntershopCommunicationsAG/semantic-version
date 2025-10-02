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

/**
 * Semantic meaning of version extension
 * <ul>
 * <li>DEV an internal version for internal testing and migration (alpha,beta,snapshot,dev)</li>
 * <li>PRE a version before the GA is happen to get feedback from stack holder (rc,cr,ea,preview)</li>
 * <li>BUILD the version extension contains a build number (it's not related to a version state) (like 'v' or '+')</li>
 * <li>NEUTRAL version extension for initialization</li>
 * <li>PLATFORM a version extension which references a required platform (jre)</li>
 * <li>GA a version which is officially supported (GA,FINAL)</li>
 * <li>POST a version which is has the same code, but may has documentation fixes (sp)</li>
 * <li>UNSPECIFIED a extension with undefined semantic (e.g. abc)</li>
 * </ul>
 */
public enum ExtensionType
{
    /**
     * internal developer version
     */
    DEV(false, true), 

    /**
     * pre release for testing purposes or preview for external stackholder
     */
    PRE(false, true), 

    /**
     * build extension (will be ignored for semantic meaning of version)
     */
    BUILD(true, true), 
    /**
     * extension type not defined yet
     */
    NEUTRAL(true, false), 

    /**
     * platform extension marker
     */
    PLATFORM(true, false), 

    /**
     * general availability marker
     */
    GA(true, true),

    /**
     * post release marker
     */
    POST(true, true), 

    /**
     * release marker with unknown semantic
     */
    UNSPECIFIED(false, true);

    private final boolean recommendedForProduction;
    private final boolean areNumbersRelevantForSorting;

    ExtensionType(boolean recommendedForProduction, boolean areNumbersRelevantForSorting)
    {
        this.recommendedForProduction = recommendedForProduction;
        this.areNumbersRelevantForSorting = areNumbersRelevantForSorting;
    }

    /**
     * Checks if this {@link ExtensionType} is recommended for production
     * @return true if the related version is recommended for production
     */
    public boolean isRecommendedForProduction()
    {
        return recommendedForProduction;
    }

    /**
     * Checks the numbers in the version extension are relevant for sorting
     * @return true numbers in the version extension are relevant for sorting (true for rc1; false for jre8)
     */
    public boolean isAreNumbersRelevantForSorting()
    {
        return areNumbersRelevantForSorting;
    }
}
