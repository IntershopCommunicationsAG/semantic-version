package com.intershop.version.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VersionResolver implements SemanticVersionResolver
{
    private static final Pattern DECIMAL_NONDECIMAL_SPLIT = Pattern.compile("^(\\d+)(\\D+)");
    private static final Pattern NONDECIMAL_DECIMAL_SPLIT = Pattern.compile("^(\\D+)(\\d+)");
    private static final Map<String, ExtensionType> MAP_EXTENSION_TO_TYPE = new HashMap<>();
    static
    {
        MAP_EXTENSION_TO_TYPE.put("local", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("snapshot", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("a", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("alpha", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("b", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("beta", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("dev", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("m", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("milestone", ExtensionType.DEV);
        MAP_EXTENSION_TO_TYPE.put("rc", ExtensionType.PRE);
        MAP_EXTENSION_TO_TYPE.put("cr", ExtensionType.PRE);
        MAP_EXTENSION_TO_TYPE.put("ea", ExtensionType.PRE);
        MAP_EXTENSION_TO_TYPE.put("preview", ExtensionType.PRE);
        MAP_EXTENSION_TO_TYPE.put("release", ExtensionType.GA);
        MAP_EXTENSION_TO_TYPE.put("+", ExtensionType.BUILD);
        MAP_EXTENSION_TO_TYPE.put("v", ExtensionType.BUILD);
        MAP_EXTENSION_TO_TYPE.put("final", ExtensionType.GA);
        MAP_EXTENSION_TO_TYPE.put("ga", ExtensionType.GA);
        MAP_EXTENSION_TO_TYPE.put("jre", ExtensionType.PLATFORM);
        MAP_EXTENSION_TO_TYPE.put("sp", ExtensionType.POST);
    }
    private static final Map<String, String> MAP_EXTENSION_TO_ITEM_EXTENSION = new HashMap<>();
    // artificial number to identify build numbers or dates as increment, which are not incrementable
    private static final Integer MAX_SEMANTIC_NUMBER = 10_000;
    static
    {
        // ga alias
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("release", "");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("final", "");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("ga", "");
        // build or version extensions
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("v", "");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("+", "");
        // dev release alias
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("a", "alpha");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("b", "beta");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("m", "milestone");
        // pre release alias
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("cr", "rc");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("preview", "rc");
        MAP_EXTENSION_TO_ITEM_EXTENSION.put("ea", "rc");
    }

    public SemanticVersion apply(String version)
    {
        int firstDash = version.indexOf("-");
        List<String> parts = Collections.emptyList();
        List<String> extensions = Collections.emptyList();
        if (firstDash > 0)
        {
            parts = splitVersion(version.substring(0, firstDash));
            extensions = splitVersion(version.substring(firstDash + 1));
        }
        else
        {
            parts = splitVersion(version);
        }
        VersionItem items = convertToItem(parts, extensions);
        return buildVersion(version, items);
    }

    List<String> splitVersion(String version)
    {
        String[] parts = version.split("[.-]");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < parts.length; i++)
        {
            String partOfVersion = parts[i].toLowerCase(Locale.US);
            Optional<Integer> optNumber = getVersionNumberSegment(partOfVersion);
            if (optNumber.isPresent())
            {
                result.add(partOfVersion);
                continue;
            }
            if (NONDECIMAL_DECIMAL_SPLIT.matcher(partOfVersion).find()
                            || DECIMAL_NONDECIMAL_SPLIT.matcher(partOfVersion).find())
            {
                result.addAll(splitWordNumber(partOfVersion));
                continue;
            }
            result.add(partOfVersion);
        }
        return result;
    }

    private List<String> splitWordNumber(String part)
    {
        List<String> result = new ArrayList<>();
        String openPart = part;
        while(!openPart.isEmpty())
        {
            Matcher matcher = NONDECIMAL_DECIMAL_SPLIT.matcher(openPart);
            if (matcher.find())
            {
                result.add(matcher.group(1));
                result.add(matcher.group(2));
                openPart = openPart.substring(matcher.group(0).length());
                continue;
            }
            matcher = DECIMAL_NONDECIMAL_SPLIT.matcher(openPart);
            if (matcher.find())
            {
                result.add(matcher.group(1));
                result.add(matcher.group(2));
                openPart = openPart.substring(matcher.group(0).length());
                continue;
            }
            result.add(openPart);
            openPart = "";
        }
        return result;
    }

    VersionItem convertToItem(List<String> parts, List<String> extensions)
    {
        VersionItem result = VersionItem.emptyVersion();
        List<String> preDashExtensions = new ArrayList<>();
        for (String currentPosition : parts)
        {
            // is that an integer for a version number
            Optional<Integer> optNumber = getVersionNumberSegment(currentPosition);

            // was an extension found before
            if (result.getExtension().isEmpty())
            {
                // is that a integer for a version number
                if (optNumber.isPresent())
                {
                    result = result.addNumber(optNumber.get());
                }
                else
                {
                    if (isNumber(currentPosition))
                    {
                        try
                        {
                            result = result.addExtensionNumber(currentPosition);
                        }
                        catch(NumberFormatException e)
                        {
                            result = result.addExtension(getReleaseType(currentPosition),
                                            getReleaseExtension(currentPosition));
                        }
                    }
                    else
                    {
                        result = result.addExtension(getReleaseType(currentPosition),
                                        getReleaseExtension(currentPosition));
                    }
                }
            }
            else
            {
                preDashExtensions.add(currentPosition);
            }
        }
        preDashExtensions.addAll(extensions);
        for (String currentPosition : preDashExtensions)
        {
            // is that an integer for a version number
            if (isNumber(currentPosition))
            {
                // extension exists - so at to extension 1.1 vs 1-1
                result = result.addExtensionNumber(currentPosition);
                // type could be NEUTRAL or GA but with a number extension this needs to be changed
                if (result.getReleaseType().equals(ExtensionType.NEUTRAL)
                                || result.getReleaseType().equals(ExtensionType.GA))
                {
                    // if extension is 0 it's not relevant so at 1 == 1-0
                    result = result.setReleaseType(isNull(currentPosition) ? ExtensionType.GA : ExtensionType.UNSPECIFIED);
                }
            }
            else
            {
                result = result.addExtension(getReleaseType(currentPosition), getReleaseExtension(currentPosition));
            }
        }
        return result;
    }

    private boolean isNull(String releaseExtension)
    {
        return releaseExtension.matches("^0+$");
    }

    private boolean isNumber(String releaseExtension)
    {
        return releaseExtension.matches("^\\d+$");
    }

    private SemanticVersion buildVersion(String version, VersionItem item)
    {
        return new VersionImpl(version, item);
    }

    private static ExtensionType getReleaseType(String lowerCased)
    {
        return MAP_EXTENSION_TO_TYPE.computeIfAbsent(lowerCased, (a) -> ExtensionType.UNSPECIFIED);
    }

    private static String getReleaseExtension(String lowerCased)
    {
        return MAP_EXTENSION_TO_ITEM_EXTENSION.computeIfAbsent(lowerCased, (a) -> a);
    }

    /**
     * @param part of version (e.g. "1.0.0.v20100202" will split to {"1","0","0","v","20100202")
     * @return a number in case the number exists and is lower than 19700101 assuming that is a data)
     */
    private static Optional<Integer> getVersionNumberSegment(String part)
    {
        try
        {
            Integer value = part.matches("^\\d+$") ? Integer.parseInt(part) : null;
            if (value != null && value < MAX_SEMANTIC_NUMBER)
            {
                return Optional.of(value);
            }
        }
        catch(NumberFormatException e)
        {
            // decimal but too large
        }
        return Optional.empty();
    }
}
