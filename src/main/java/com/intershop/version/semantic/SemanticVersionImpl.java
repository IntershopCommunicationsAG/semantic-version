package com.intershop.version.semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Straight forward implementation of semantic versions without version extensions
 */
class SemanticVersionImpl implements SemanticVersion
{
    private final String version;
    private final VersionItem item;

    /**
     * @param version
     * @param increment
     * @param releaseType
     * @param isIncrementable parser decides that there are additional number which can't be incremented
     */
    public SemanticVersionImpl(String version, VersionItem item)
    {
        this.version = version;
        this.item = item;
    }

    @Override
    public int getMajor()
    {
        return item.getNumbers().size() > 0 ? item.getNumbers().get(0) : 0;
    }

    @Override
    public int getMinor()
    {
        return item.getNumbers().size() > 1 ? item.getNumbers().get(1) : 0;
    }

    @Override
    public int getPatch()
    {
        return item.getNumbers().size() > 2 ? item.getNumbers().get(2) : 0;
    }

    public List<Integer> getNumbers()
    {
        return item.getNumbers();
    }

    public List<VersionExtensionItem> getExtensions()
    {
        return item.getExtensions();
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return item.toString();
    }

    @Override
    public ExtensionType getReleaseType()
    {
        return item.getReleaseType();
    }

    @Override
    public String getExtension()
    {
        return item.getExtension();
    }

    @Override
    public int compareTo(SemanticVersion version)
    {
        if (version instanceof SemanticVersionImpl)
        {
            return VersionComparators.VERSION_IMPL_COMPARATOR.compare(this, ((SemanticVersionImpl)version));
        }
        return VersionComparators.SEMVER_MMP_COMPARATOR.compare(this, version);
    }

    @Override
    public Optional<String> getPlatformExtension()
    {
        List<String> result = item.getExtensions()
                                  .stream()
                                  .filter(e -> ExtensionType.PLATFORM.equals(e.getExtensionType()))
                                  .map(e -> "-" + e.toString())
                                  .collect(Collectors.toList());
        if (result.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(String.join("-", result));
    }

    @Override
    public String getVersionWithoutBuildExtension()
    {
        String result = SemanticVersion.super.getVersionWithoutBuildExtension();
        List<Integer> numbers = item.getNumbers();
        if (numbers.size() > 3)
        {
            StringBuilder b = new StringBuilder(result);
            for (int i = 3; i < numbers.size(); i++)
            {
                b = b.append(".").append(numbers.get(i));
            }
            result = b.toString();
        }
        return result;
    }

    @Override
    public SemanticVersion incrementLastIncrement()
    {
        List<VersionExtensionItem> extensions = item.getExtensions();
        List<VersionExtensionItem> newExtensions = new ArrayList<>(extensions.size());
        boolean incrementIsOpen = true;
        for(int i=extensions.size() - 1; i >= 0; i--)
        {
            VersionExtensionItem extension = extensions.get(i);
            if (incrementIsOpen)
            {
                ExtensionType type = extension.getExtensionType();
                if (ExtensionType.PRE.equals(type) || ExtensionType.DEV.equals(type))
                {
                    extension = extension.incrementNumber();
                }
            }
            newExtensions.add(0, extension);
        }
        VersionItem newItem = VersionItem.emptyVersion();
        for(Integer number:item.getNumbers())
        {
            newItem = newItem.addNumber(number);
        }
        newItem = newItem.addExtensions(newExtensions);
        return new SemanticVersionImpl(newItem.toString(), newItem);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(version);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SemanticVersionImpl other = (SemanticVersionImpl)obj;
        return Objects.equals(version, other.version);
    }

}
