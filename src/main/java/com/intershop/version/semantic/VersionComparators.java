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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ComparatorUtils;

/**
 * Compares (lowest first) two {@link SemanticVersion} by (ordered)
 * <ul>
 * <li>major version</li>
 * <li>minor version</li>
 * <li>patch version</li>
 * <li>{@link SemanticVersion#getReleaseType()} ({@link ExtensionType})</li>
 * <li>{@link SemanticVersion#getExtension()}</li>
 * </ul>
 */
class VersionComparators
{
    private static final Function<String, SemanticVersion> RESOLVER = new VersionResolver();

    /*
     * Comparer via SemanticVersion interface
     */
    private static final Comparator<SemanticVersion> MAJOR_COMPARATOR = (a, b) -> {
        return a.getMajor() - b.getMajor();
    };
    private static final Comparator<SemanticVersion> MINOR_COMPARATOR = (a, b) -> {
        return a.getMinor() - b.getMinor();
    };
    private static final Comparator<SemanticVersion> PATCH_COMPARATOR = (a, b) -> {
        return a.getPatch() - b.getPatch();
    };
    private static final Comparator<SemanticVersion> SEMVER_RELEASE_TYPE_COMPARATER = (a, b) -> {
        return a.getReleaseType().compareTo(b.getReleaseType());
    };
    @SuppressWarnings("unchecked")
    public static final Comparator<SemanticVersion> SEMVER_MMP_COMPARATOR = ComparatorUtils.chainedComparator(
                    MAJOR_COMPARATOR, MINOR_COMPARATOR, PATCH_COMPARATOR, SEMVER_RELEASE_TYPE_COMPARATER);

    /**
     * Comparator SemanticVersion just calls a.compareTo(b)
     */
    public static final Comparator<SemanticVersion> VERSION_COMPARATOR = (a,b) -> a.compareTo(b);

    public static final Comparator<String> STRING_COMPARATOR = (a, b) -> {
        return VERSION_COMPARATOR.compare(RESOLVER.apply(a), RESOLVER.apply(b));
    };
    
    /*
     * Comparer for VersionImpl implementation of SemanticVersion (can sort deeper)
     */
    private static final Comparator<VersionImpl> VERSION_NUMBERS_COMPARATER = (a, b) -> {
        int amountOfNumber = Math.min(a.getNumbers().size(), b.getNumbers().size());
        for (int i = 0; i < amountOfNumber; i++)
        {
            int result = a.getNumbers().get(i).compareTo(b.getNumbers().get(i));
            if (result != 0)
            {
                return result;
            }
        }
        int maxOfNumber = Math.max(a.getNumbers().size(), b.getNumbers().size());
        boolean aIsLonger = a.getNumbers().size() == maxOfNumber;
        for (int i = amountOfNumber; i < maxOfNumber; i++)
        {
            // 1.0 == 1
            if ((aIsLonger ? a : b).getNumbers().get(i) != 0)
            {
                return aIsLonger ? 1 : -1;
            }
        }
        return 0;
    };

    private static final Comparator<VersionImpl> VERSION_RELEASE_TYPE_COMPARATER = (a, b) -> {
        return SEMVER_RELEASE_TYPE_COMPARATER.compare(a, b);
    };

    private static final Comparator<VersionExtensionItem> EXTENSION_RELEASE_TYPE_COMPARATER = (a, b) -> {
        return a.getExtensionType().compareTo(b.getExtensionType());
    };

    /**
     * Compares numbers of extension, in case of platform extension the numbers are not relevant
     */
    private static final Comparator<VersionExtensionItem> EXTENSION_NUMBER_COMPARATOR = (a, b) -> {
        int amountOfNumber = Math.min(a.getNumbers().size(), b.getNumbers().size());
        for (int i = 0; i < amountOfNumber; i++)
        {
            int result = a.getNumbers().get(i).compareTo(b.getNumbers().get(i));
            if (result != 0)
            {
                return result;
            }
        }
        int maxOfNumber = Math.max(a.getNumbers().size(), b.getNumbers().size());
        boolean aIsLonger = a.getNumbers().size() == maxOfNumber;
        for (int i = amountOfNumber; i < maxOfNumber; i++)
        {
            // 1.0 == 1
            if (!(aIsLonger ? a : b).getNumbers().get(i).isNull())
            {
                return aIsLonger ? 1 : -1;
            }
        }
        return 0;
    };
    private static final Comparator<VersionExtensionItem> EXTENSION_STRING_COMPARATER = (a, b) -> {
        return a.getExtension().compareTo(b.getExtension());
    };
    // one extension can contain string others a number: "2.0.1-xyz" < "2.0.1-123";
    private static final Comparator<VersionExtensionItem> EXTENSION_STRING_OR_NUMBER_COMPARATER = (a, b) -> {
        if (a.getExtension().isEmpty() && b.getExtension().isEmpty()
                        || !a.getExtension().isEmpty() && !b.getExtension().isEmpty())
        {
            return 0;
        }
        return a.getExtension().isEmpty() ? 1 : -1;
    };
    @SuppressWarnings("unchecked")
    public static final Comparator<VersionExtensionItem> EXTENSION_COMPARATOR = ComparatorUtils.chainedComparator(
                    EXTENSION_RELEASE_TYPE_COMPARATER, EXTENSION_STRING_OR_NUMBER_COMPARATER, EXTENSION_STRING_COMPARATER,
                    EXTENSION_NUMBER_COMPARATOR);

    /**
     * Compares list of extensions, step by step
     */
    public static final Comparator<List<VersionExtensionItem>> LIST_VERSION_EXTENSION_ITEM_COMPARATER = (aAll, bAll) -> {
        List<VersionExtensionItem> a = aAll.stream().filter(e -> e.getExtensionType().isAreNumbersRelevantForSorting()).collect(Collectors.toList());
        List<VersionExtensionItem> b = bAll.stream().filter(e -> e.getExtensionType().isAreNumbersRelevantForSorting()).collect(Collectors.toList());
        int numberOfExtensions = Math.min(a.size(), b.size());
        for (int i = 0; i < numberOfExtensions; i++)
        {
            int result = EXTENSION_COMPARATOR.compare(a.get(i), b.get(i));
            if (result != 0)
            {
                return result;
            }
        }
        if (a.size() == b.size())
        {
            return 0;
        }
        int negator = a.size() < b.size() ? -1 : 1;
        List<VersionExtensionItem> additionalExtension = a.size() < b.size() ? b.subList(numberOfExtensions, b.size())
                        : a.subList(numberOfExtensions, a.size());
        List<VersionExtensionItem> nonNeutralExtensions = additionalExtension.stream()
                                                                             .filter(e -> !ExtensionType.GA.equals(
                                                                                             e.getExtensionType()))
                                                                             .collect(Collectors.toList());
        // is other pre or post release added
        Optional<ExtensionType> nonNeutralReleaseOpt = nonNeutralExtensions.stream()
                                                                         .map(VersionExtensionItem::getExtensionType)
                                                                         .findFirst();
        int result = nonNeutralReleaseOpt.isEmpty() ? 0 : nonNeutralReleaseOpt.get().compareTo(ExtensionType.GA);

        // are number added
        if (result == 0 && !additionalExtension.get(0).getNumbers().isEmpty())
        {
            // added numbers must be > 0 ( 1-rc == 1-rc-0 )
            for (VersionNumberItems.Item item : additionalExtension.get(0).getNumbers())
            {
                if (!item.isNull())
                {
                    result = 1;
                    break;
                }
            }
        }
        return negator * result;
    };
    private static final Comparator<VersionImpl> VERSION_EXTENSION_COMPARATER = (a, b) -> {
        return LIST_VERSION_EXTENSION_ITEM_COMPARATER.compare(a.getExtensions(), b.getExtensions());
    };

    @SuppressWarnings("unchecked")
    public static final Comparator<VersionImpl> VERSION_IMPL_COMPARATOR = ComparatorUtils.chainedComparator(
                    VERSION_NUMBERS_COMPARATER, VERSION_RELEASE_TYPE_COMPARATER,
                    VERSION_EXTENSION_COMPARATER);
}
