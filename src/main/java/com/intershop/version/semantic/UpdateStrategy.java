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
 * Defines several type of update strategy e.g. one might only want to update to a compatible version so he/she would
 * use {@link #MINOR}. The meanings are as follows:
 * <ul>
 *     <li>{@link #MAJOR}: allow updates up to the major version</li>
 *     <li>{@link #MINOR}: allow updates up to the minor version</li>
 *     <li>{@link #PATCH}: allow updates up to the patch version</li>
 *     <li>{@link #DEV}: allow update to newest (also non production ready) version</li>
 *     <li>{@link #STICK}: allow <b>no</b> updates at all</li>
 * </ul>
 */
public enum UpdateStrategy
{
    /**
     * Allows or includes major (incompatible) updates
     */
    MAJOR,

    /**
     * Allows or includes minor (compatible) updates; compatible on update to newer (this) version, but not compatible for updates to an older version.
     */
    MINOR,

    /**
     * Allows or includes patch (compatible) updates
     */
    PATCH,

    /**
     * Allows or includes updates to non-production ready versions
     */
    DEV,

    /**
     * Don't allow updates
     */
    STICK;
}
