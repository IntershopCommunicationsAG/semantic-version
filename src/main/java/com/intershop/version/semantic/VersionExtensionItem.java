package com.intershop.version.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class VersionExtensionItem
{
    public static VersionExtensionItem emptyVersion()
    {
        return new VersionExtensionItem(ExtensionType.NEUTRAL, "", Collections.emptyList());
    }

    private final ExtensionType extensionType;
    private final String extension;
    private final List<VersionNumberItems.Item> numbers;

    private VersionExtensionItem(ExtensionType type, String extension, List<VersionNumberItems.Item> numbers)
    {
        this.extensionType = type;
        this.extension = extension;
        this.numbers = numbers;
    }

    public String getExtension()
    {
        return extension;
    }

    List<VersionNumberItems.Item> getNumbers()
    {
        return numbers;
    }

    public ExtensionType getExtensionType()
    {
        return extensionType;
    }

    public VersionExtensionItem setExtensionType(ExtensionType type)
    {
        return new VersionExtensionItem(type, extension, numbers);
    }

    public VersionExtensionItem setExtension(String extension)
    {
        return new VersionExtensionItem(extensionType, extension, numbers);
    }

    public VersionExtensionItem addNumber(String number)
    {
        List<VersionNumberItems.Item> result = new ArrayList<>(numbers);
        result.add(VersionNumberItems.parseItem(number));
        return new VersionExtensionItem(extensionType, extension, Collections.unmodifiableList(result));
    }

    @Override
    public String toString()
    {
        return extension + String.join(".", numbers.stream().map(VersionNumberItems.Item::toString).collect(Collectors.toList()));
    }

    /**
     * @return true if the version extensions is still empty. The version resolver is using that state to add data for
     *         extension in different orders.
     */
    public boolean isEmpty()
    {
        return numbers.isEmpty() && extension.isEmpty();
    }

    public VersionExtensionItem incrementNumber()
    {
        List<VersionNumberItems.Item> result = new ArrayList<>(numbers);
        if (result.isEmpty())
        {
            result.add(VersionNumberItems.parseItem("1"));
        }
        else
        {
            int lastPos = result.size() - 1;
            result.set(lastPos, result.get(lastPos).increment());
        }
        return new VersionExtensionItem(extensionType, extension, result);
    }
}
