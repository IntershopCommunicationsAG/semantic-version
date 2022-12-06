package com.intershop.version.semantic;

/**
 * Defines several type of update strategy e.g. one might only want to update to a compatible version so he/she would
 * use {@link #MINOR}. The meanings are as follows:
 * <ul>
 *     <li>{@link #MAJOR}: allow updates up to the major version</li>
 *     <li>{@link #MINOR}: allow updates up to the minor version</li>
 *     <li>{@link #PATCH}: allow updates up to the patch version</li>
 *     <li>{@link #INC}: allow all updates</li>
 *     <li>{@link #STICK}: allow <b>no</b> updates at all</li>
 * </ul>
 */
public enum UpdateStrategy
{
    MAJOR,
    MINOR,
    PATCH,
    INC,
    STICK;
}
