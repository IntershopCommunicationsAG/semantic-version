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
