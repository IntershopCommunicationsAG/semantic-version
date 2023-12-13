package com.intershop.version.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class VersionItem
{
    public static VersionItem emptyVersion()
    {
        return new VersionItem(Collections.emptyList(), Collections.emptyList());
    }

    private final List<Integer> numbers;
    private final List<VersionExtensionItem> extensions;

    private VersionItem(List<Integer> numbers, List<VersionExtensionItem> extensions)
    {
        this.numbers = numbers;
        this.extensions = extensions;
    }

    public ExtensionType getReleaseType()
    {
        // find first non neutral or it's a GA
        return extensions.stream()
                         .map(VersionExtensionItem::getExtensionType)
                         .filter(t -> !ExtensionType.NEUTRAL.equals(t))
                         .filter(t -> !ExtensionType.BUILD.equals(t))
                         .filter(t -> !ExtensionType.PLATFORM.equals(t))
                         .findFirst()
                         .orElse(ExtensionType.GA);
    }

    public List<Integer> getNumbers()
    {
        return numbers;
    }

    public List<VersionExtensionItem> getExtensions()
    {
        return extensions;
    }

    public VersionItem addNumber(Integer number)
    {
        ArrayList<Integer> result = new ArrayList<>(numbers);
        result.add(number);
        return new VersionItem(Collections.unmodifiableList(result), extensions);
    }

    /**
     * @param number as string because the number can be huge
     * @return
     */
    public VersionItem addExtensionNumber(String number)
    {
        VersionExtensionItem item = extensions.isEmpty() ? VersionExtensionItem.emptyVersion()
                        : extensions.get(extensions.size() - 1);
        List<VersionExtensionItem> newExtensions = new ArrayList<>(extensions);
        if (!newExtensions.isEmpty())
        {
            newExtensions = newExtensions.subList(0, newExtensions.size() - 1);
        }
        newExtensions.add(item.addNumber(number));
        return new VersionItem(numbers, Collections.unmodifiableList(newExtensions));
    }

    @Override
    public String toString()
    {
        return getNumbersAsString() + (hasExtensions() ? "-" + getExtension() : "");
    }

    public boolean isEmpty()
    {
        return numbers.isEmpty() && !hasExtensions();
    }

    public boolean hasExtensions()
    {
        return !extensions.isEmpty();
    }

    private String getNumbersAsString()
    {
        return String.join(".", numbers.stream().map(n -> n.toString()).collect(Collectors.toList()));
    }

    public String getExtension()
    {
        return String.join("-", extensions.stream().map(VersionExtensionItem::toString).collect(Collectors.toList()));
    }

    public VersionItem setReleaseType(ExtensionType releaseType)
    {
        VersionExtensionItem item = VersionExtensionItem.emptyVersion().setExtensionType(releaseType);
        List<VersionExtensionItem> newExtensions = new ArrayList<>(extensions);
        if (hasExtensions())
        {
            VersionExtensionItem lastItem = extensions.get(extensions.size() - 1);
            item = ExtensionType.NEUTRAL.equals(lastItem.getExtensionType()) ? lastItem.setExtensionType(releaseType) : lastItem;
            newExtensions = new ArrayList<>(extensions.subList(0, extensions.size() - 1));
        }
        newExtensions.add(item);
        return new VersionItem(numbers, Collections.unmodifiableList(newExtensions));
    }

    public VersionItem addExtension(ExtensionType releaseType, String extension)
    {
        VersionExtensionItem item = VersionExtensionItem.emptyVersion()
                                                        .setExtension(extension)
                                                        .setExtensionType(releaseType);
        List<VersionExtensionItem> newExtensions = new ArrayList<>(extensions);
        newExtensions.add(item);
        return new VersionItem(numbers, Collections.unmodifiableList(newExtensions));
    }

    public VersionItem addExtensions(List<VersionExtensionItem> newExtensions)
    {
        List<VersionExtensionItem> result = new ArrayList<>(extensions);
        result.addAll(newExtensions);
        return new VersionItem(numbers, Collections.unmodifiableList(result));
    }
}
