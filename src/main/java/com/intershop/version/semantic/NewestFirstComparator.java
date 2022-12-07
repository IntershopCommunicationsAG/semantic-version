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

import java.util.Comparator;

/**
 * Compares (lowest first) two {@link SemanticVersion} by (ordered)
 * <ul>
 *     <li>major version</li>
 *     <li>minor version</li>
 *     <li>patch version</li>
 *     <li>{@link SemanticVersion#getIncrementState()} ({@link ReleaseType})</li>
 *     <li>{@link SemanticVersion#getIncrement()}</li>
 * </ul>
 */
public class NewestFirstComparator implements Comparator<SemanticVersion>
{
    @Override
    public int compare(SemanticVersion o1, SemanticVersion o2)
    {
        int majorDiff = o1.getMajor() - o2.getMajor();
        if (majorDiff != 0)
        {
            return majorDiff;
        }
        int minorDiff = o1.getMinor() - o2.getMinor();
        if (minorDiff != 0)
        {
            return minorDiff;
        }
        int patchDiff = o1.getPatch() - o2.getPatch();
        if (patchDiff != 0)
        {
            return patchDiff;
        }
        if (o2.getIncrementState() == null || o1.getIncrementState() == null)
        {
            if (o1.getIncrementState() != null)
            {
                return o1.getIncrementState().ordinal();
            }
            if (o2.getIncrementState() != null)
            {
                return - o2.getIncrementState().ordinal();
            }
        }
        else
        {
            int patchState = o1.getIncrementState().compareTo(o2.getIncrementState());
            if (patchState != 0)
            {
                return patchState;
            }
        }
        return o1.getIncrement() - o2.getIncrement();
    }
}
