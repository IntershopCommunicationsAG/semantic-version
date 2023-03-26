package com.intershop.version.semantic;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * SemanticVersionIncrementor can increment a semantic version for automatic versioning. The incrementor consume one or
 * multiple versions. The reason for multiple versions are merges of branches, which leads to multiple possible latest
 * releases on different branches.
 * <ul>
 * <li>parent version is 1.0.0 the incrementor results with 1.0.1 with strategy patch.</li>
 * <li>parent versions are (1.0.0, 1.1.0) the incrementor results with 1.1.1 with strategy patch.</li>
 * </ul>
 */
public class SemanticVersionIncrementor
{
    private static final String FIRST_PRE_RELEASE = "-alpha1";
    private final Comparator<String> comparator;
    private final Function<String, SemanticVersion> resolver;

    /**
     * Constructor with predefined version resolver and comparator
     */
    public SemanticVersionIncrementor()
    {
        this(new VersionResolver(), VersionComparators.STRING_COMPARATOR);
    }

    /**
     * Constructor with extenal defined resolver and comparator
     * @param resolver converts a string to a semantic version
     * @param comparator can sort versions
     */
    public SemanticVersionIncrementor(Function<String, SemanticVersion> resolver, Comparator<String> comparator)
    {
        this.comparator = comparator;
        this.resolver = resolver;
    }

    /**
     * @param oldVersions list of version, related to the current commit
     * @param strategy fallback update strategy in case the newest listed version is not a pre or dev release
     * @return a version string, which can be used for up-comming releases.
     */
    public String incrementForRelease(List<String> oldVersions, UpdateStrategy strategy)
    {
        if (oldVersions.isEmpty())
        {
            return "1.0.0";
        }
        return oldVersions.stream().max(comparator).map(v -> incrementForRelease(v, strategy)).get();
    }

    String incrementForRelease(String lastestVersion, UpdateStrategy strategy)
    {
        SemanticVersion semVer = resolver.apply(lastestVersion);
        return isPreRelease(semVer.getReleaseType()) 
                        ? incrementForReleaseWithPreReleases(semVer, strategy)
                        : incrementForReleaseWithoutPreReleases(semVer, strategy);
    }

    private boolean isPreRelease(ExtensionType releaseType)
    {
        boolean result = false;
        switch(releaseType)
        {
            case DEV:
            case PRE:
                result = true;
            default:
                break;
        }
        return result;
    }

    private String incrementForReleaseWithoutPreReleases(SemanticVersion semVer, UpdateStrategy strategy)
    {
        StringBuilder result = new StringBuilder();
        switch(strategy)
        {
            case MAJOR:
                result = result.append(semVer.getMajor() + 1).append(".0.0");
                break;
            case MINOR:
                result = result.append(semVer.getMajor()).append(".").append(semVer.getMinor() + 1).append(".0");
                break;
            case PATCH:
                result = result.append(semVer.getMajor())
                               .append(".")
                               .append(semVer.getMinor())
                               .append(".")
                               .append(semVer.getPatch() + 1);
                break;
            case DEV:
            case STICK:
            default:
                throw new IllegalArgumentException("Unknown or useless meaning provided " + strategy);
        }
        Optional<String> extension = semVer.getPlatformExtension();
        result = extension.isEmpty() ? result : result.append(extension.get());
        return result.toString();
    }

    private String incrementForReleaseWithPreReleases(SemanticVersion semVer, UpdateStrategy strategy)
    {
        StringBuilder result = new StringBuilder();
        result = result.append(semVer.getMajor())
                       .append(".")
                       .append(semVer.getMinor())
                       .append(".")
                       .append(semVer.getPatch());
        Optional<String> extension = semVer.getPlatformExtension();
        result = extension.isEmpty() ? result : result.append(extension.get());
        return result.toString();
    }

    /**
     * @param oldVersions list of version, related to the current commit
     * @param strategy fallback update strategy in case the newest listed version is not a pre or dev release
     * @return a version string, which can be used for up-comming pre- or dev- releases. It will increment the last number of pre release extension. if not pre release extension will be found "alpha1" is added.
     */
    public String incrementForPreRelease(List<String> oldVersions, UpdateStrategy strategy)
    {
        if (oldVersions.isEmpty())
        {
            return "1.0.0" + FIRST_PRE_RELEASE;
        }
        return oldVersions.stream().max(comparator).map(v -> incrementForPreRelease(v, strategy)).get();
    }

    private String incrementForPreRelease(String lastestVersion, UpdateStrategy strategy)
    {
        SemanticVersion semVer = resolver.apply(lastestVersion);
        if (semVer.isRecommendedForProduction())
        {
            return incrementForRelease(lastestVersion, strategy) + FIRST_PRE_RELEASE;
        }
        return semVer.incrementLastIncrement().toString();
    }

}
