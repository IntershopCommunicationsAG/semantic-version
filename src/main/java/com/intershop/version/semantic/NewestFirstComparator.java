package com.intershop.version.semantic;

import java.util.Comparator;

/**
 * Compares (highest first) two {@link SemanticVersion} by (ordered)
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
        int majorDiff = o2.getMajor() - o1.getMajor();
        if (majorDiff != 0)
        {
            return majorDiff;
        }
        int minorDiff = o2.getMinor() - o1.getMinor();
        if (minorDiff != 0)
        {
            return minorDiff;
        }
        int patchDiff = o2.getPatch() - o1.getPatch();
        if (patchDiff != 0)
        {
            return patchDiff;
        }
        if (o2.getIncrementState() == null || o1.getIncrementState() == null)
        {
            if (o1.getIncrementState() != null)
            {
                return - o1.getIncrementState().ordinal();
            }
            if (o2.getIncrementState() != null)
            {
                return o2.getIncrementState().ordinal();
            }
        }
        else
        {
            int patchState = o2.getIncrementState().compareTo(o1.getIncrementState());
            if (patchState != 0)
            {
                return patchState;
            }
        }
        return o2.getIncrement() - o1.getIncrement();
    }
}
