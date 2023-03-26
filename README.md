[![Build Status](https://github.com/IntershopCommunicationsAG/sementic-version/actions/workflows/build.yml/badge.svg)](https://github.com/IntershopCommunicationsAG/sementic-version/actions/workflows/build.yml)

# Introduction

This project targets the handling of (possibly) [semantic versions](https://semver.org/). Besides semantic versions this project also supports some derived versions that are used by various libraries.
* [Semantic Versions v1.0.0](https://semver.org/spec/v1.0.0.html) declares one extension as pre-release in lexicographic ASCII order.
* [Semantic Versions v2.0.0](https://semver.org/spec/v2.0.0.html) allows one extension and one build extension (both are optional).
* [Maven Specification](https://maven.apache.org/pom.html#Version_Order_Specification) allows multiple extensions including post-release and GA extensions
* [Maven Version Sorting](https://maven.apache.org/ref/3.8.5/maven-artifact/apidocs/org/apache/maven/artifact/versioning/ComparableVersion.html) contains alias for pre-releases with same semantic meaning (e.g 1.0.0-a1 is same as 1.0.0-alpha1), all unknown extensions are recognized as post-releases.

Unfortunatelly, the build extension is not used to declare:
* platform specific artifacts. e.g. for different jre (bytecodes) like -jre8 or -jre11, or for docker -amd64
* major dependencies, e.g. -jakarta or -centos

# Syntax

The general syntax is comparable to SemVer.
`<major>.<minor>.<patch>(-<extension>)?(+<build extension>)?`

Allows multiple extensions and more than three numbers.
`<major>.<minor>.<patch>(.<increment>)*(-<extension>)*`

Allows to remove trailing zeros.
`<major>(.<minor>(.<patch>)?)?(.<increment>)*(-<extension>)*`

```
# 1.0.0-GA+20230101-56789
<valid semver> ::= <numberic version>
                 | <numberic version> <extensions>

# 1.12.0
<numberic version> ::= <digits>
                     | <digits> "." <digits>

<extensions> ::= "-" <release-extension>
               | "+" <build-extension>
               | "-" <release-extension> <extensions>
               | "+" <build-extension> <extensions>

# 123 or 0815
<digits> ::= <digit>
           | <digit> <digits>

<digit> ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"

# feature_2343
<letters> ::= <letter>
           | <letter> <letters>
           | <letter> "_" <letters>

<letter> ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J"
           | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T"
           | "U" | "V" | "W" | "X" | "Y" | "Z" | "a" | "b" | "c" | "d"
           | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n"
           | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x"
           | "y" | "z"
```

Following `release-extension` are regonized with special semantic meaning:

DEV-Release extensions
  * LOCAL
  * DEV
  * SNAPSHOT
  * MILESTONE (m)

PRE-Release extensions
  * ALPHA (a)
  * BETA (b)
  * PREVIEW (ea, rc, cr)

GA-Release extensions
  * FINAL
  * GA
  * RELEASE

POST-Release extensions
  * SP

Build extensions
  * v
  * (separated with +)

Platform extensions
  * jre

### Examples

| Example     | Major | Minor | Patch | Extentension Type |
|-------------|-------|-------|-------|----------------|
| 1.2.3       | 1     | 2     | 3     | GA             |
| 4.5.6-FINAL | 4     | 5     | 6     | GA             |
| 4.5.6-GA    | 4     | 5     | 6     | GA             |
| 4.5.6-RC1   | 4     | 5     | 6     | RC             |
| 4.5.6-DEV3  | 4     | 5     | 6     | DEV            |
| 1.2.3.4.5.6 | 1     | 2     | 3     | GA             |
| 4.5.6-RC1-jre8 | 4  | 5     | 6     | RC             |

# Usage
## Creation

Creating a semantic version object
```java
SemanticVersion.valueOf("1.2.3")
```


## Comparing
```java
semanticVersion1.compareTo(semanticVersion2)
```

Compares two versions with semantic meaning. This leads to following outcome.

| Outcome | State                                | Meaning |
|---------|--------------------------------------|---------|
| `< 0`   | semanticVersion1 < semanticVersion2  | semanticVersion2 is greater/newer |
| `== 0`  | semanticVersion1 == semanticVersion2 | same semantic meaning |
| `> 0`   | semanticVersion1 > semanticVersion2  | semanticVersion1 is greater/newer |

## Finding an appropriate version
`SemanticVersionComparer.getNewestVersion` filters the newest possible version from a given list of versions.

Imagine the current version is
 * `1.2.3`

and there are updates available
 * `1.2.4`
 * `1.3.0`
 * `2.0.1`
 * `2.0.1-RC1`

the following code will find the appropriate update version matching your desired update strategy:
```java
SemanticVersionComparer.getNewestVersion(updateStrategy, List.of("1.2.4", "1.3.0", "2.0.1", "2.0.2-RC1"), "1.2.3")
```

with the following outcome

| UpdateStrategy | Outcome   |
|----------------|-----------|
| MAJOR          | 2.0.1     |
| MINOR          | 1.3.0     |
| PATCH          | 1.2.4     |
| INC            | 2.0.2-RC1 |
| STICK          | 1.2.3     |

# License

Copyright 2014-2022 Intershop Communications.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
