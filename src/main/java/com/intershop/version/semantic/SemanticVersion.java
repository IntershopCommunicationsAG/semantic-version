package com.intershop.version.semantic;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticVersion implements Comparable<SemanticVersion>
{
    private static final Comparator<SemanticVersion> COMPARATOR_NEWEST_FIRST = new NewestFirstComparator();
    private static final Pattern INCREMENT_PATTERN = Pattern.compile("^(\\D+)(\\d+)$");
    private static final Pattern JETTY_GA_PATTERN = Pattern.compile("^v\\d{8}$");

    /**
     * Creates a new {@link SemanticVersion} out of a given version string
     * @param versionString the version string
     * @return the created {@link SemanticVersion}
     */
    public static SemanticVersion valueOf(String versionString)
    {
        try
        {
            return new SemanticVersion(versionString);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't create semantic version object for version '"+versionString+"'.");
        }
    }

    /**
     * Creates a new {@link SemanticVersion} out version components
     * @param major the major component
     * @param minor the minor component
     * @param patch the patch component
     */
    public static SemanticVersion valueOf(int major, int minor, int patch)
    {
        return valueOf("" + major + "." + minor + "." + patch);
    }

    private final String version;
    private final String versionWithoutBuild;
    private final boolean isSemantic;
    private final int major;
    private final int minor;
    private final int patch;
    private final int increment;
    private ReleaseType incrementState = ReleaseType.GA;
    private boolean isIncrementable = true;

    private SemanticVersion(String version)
    {
        this.version = version;
        String[] versionAndBuild = version.split("-");
        versionWithoutBuild= versionAndBuild[0];
        String[] parts = version.split("[.-]");
        boolean foundIncrementStateAlone = false;
        int[] numbers = { 0, 0, 0, 0 };
        try
        {
            if (parts.length < 1)
            {
                isIncrementable = false;
            }
            else
            {
                // digits for all except last position
                for (int i = 0; i < parts.length; i++)
                {
                    // are there no numbers
                    ReleaseType localSate = getIncrementState(parts[i].toLowerCase());
                    if (localSate != null)
                    {
                        incrementState = localSate;
                        foundIncrementStateAlone = true;
                    }
                    else
                    {
                        Matcher matcher = INCREMENT_PATTERN.matcher(parts[i]);
                        if (matcher.find()) {
                            incrementState = getIncrementState(matcher.group(1).toLowerCase());
                            String someNumberStr = matcher.group(2);
                            if (!JETTY_GA_PATTERN.matcher(parts[i]).find())
                            {
                                numbers[3] = Integer.parseInt(someNumberStr);
                            }
                        }
                        else
                        {
                            if (i>3)
                            {
                                isIncrementable = false;
                                break;
                            }
                            Integer value = getDigit(parts[i]);
                            if (value == null)
                            {
                                isIncrementable = false;
                                break;
                            }
                            if (foundIncrementStateAlone)
                            {
                                numbers[i - 1] = value;
                            }
                            else
                            {
                                numbers[i] = value;
                            }
                        }
                    }
                }
            }
        }
        catch(NumberFormatException e)
        {
            isIncrementable = false;
        }
        major = numbers[0];
        minor = numbers[1];
        patch = numbers[2];
        increment = numbers[3];
        this.isSemantic = version.equals(major + "." + minor + "." + patch);
    }

    private static ReleaseType getIncrementState(String lowerCased)
    {
        if ("final".equals(lowerCased) || "ga".equals(lowerCased))
        {
            return ReleaseType.GA;
        }
        if ("rc".equals(lowerCased))
        {
            return ReleaseType.RC;
        }
        if ("dev".equals(lowerCased))
        {
            return ReleaseType.DEV;
        }
        if (JETTY_GA_PATTERN.matcher(lowerCased).find())
        {
            return ReleaseType.GA;
        }
        return null;
    }

    private static Integer getDigit(String part)
    {
        Integer value = part.matches("^\\d+$") ? Integer.parseInt(part) : null;
        if (value != null && value < 1000)
        {
            return value;
        }
        return null;
    }

    /**
     * @return {@code true} if the number schema allows sorting
     */
    public boolean isIncrementable()
    {
        return isIncrementable;
    }

    /**
     * @return {@code true} if the number follows the semantic version schema
     */
    public boolean isSemantic()
    {
        return isSemantic;
    }

    /**
     * Returns the major version component
     * @return the major version component
     */
    public int getMajor()
    {
        return major;
    }

    /**
     * Returns the minor version component
     * @return the minor version component
     */
    public int getMinor()
    {
        return minor;
    }

    /**
     * Returns the patch version component
     * @return the patch version component
     */
    public int getPatch()
    {
        return patch;
    }

    /**
     * Returns the increment version component (e.g. 1.2.3-dev3 = third dev release)
     * @return the major version component
     */
    public int getIncrement()
    {
        return increment;
    }

    /**
     * Returns the version string that had been used to create this instance
     * @return the version string that had been used to create this instance
     */
    public String getVersion()
    {
        return version;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return version.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SemanticVersion other = (SemanticVersion)obj;
        if (version == null)
        {
            if (other.version != null)
                return false;
        }
        else if (!version.equals(other.version))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SemanticVersion [version=" + version + "]";
    }

    /**
     * Returns the increment state (release type) of this version
     * @return the increment state (release type) of this version
     */
    public ReleaseType getIncrementState()
    {
        return incrementState;
    }

    /**
     * Returns a string representation if this version except the incrementState+increment components
     * @return a string representation if this version except the incrementState+increment components
     */
    public String getVersionWithoutBuildExtension()
    {
        if (isSemantic)
        {
            return getVersion();
        }
        return versionWithoutBuild;
    }

    /**
     * {@inheritDoc}<br/>
     * Uses the {@link NewestFirstComparator} for comparison.
     */
    @Override
    public int compareTo(SemanticVersion o)
    {
        return COMPARATOR_NEWEST_FIRST.compare(this, o);
    }
}
