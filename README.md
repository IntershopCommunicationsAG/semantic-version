[![Build Status](https://github.com/IntershopCommunicationsAG/sementic-version/actions/workflows/build.yml/badge.svg)](https://github.com/IntershopCommunicationsAG/sementic-version/actions/workflows/build.yml)

# Introduction

This project targets the handling of (possibly) [semantic versions](https://semver.org/). Besides semantic versions this project also supports some derived versions that are used by various libraries.

# Usage
## Syntax

The supported syntax follows this pattern:

`<major>.<minor>.<patch>-<incrementState><increment>`

For the `incrementState` the following values (case-insensitive) are supported:

  * FINAL
  * GA (default)
  * RC
  * DEV

Besides this syntax even more version components are supported:

`<major>.<minor>.<patch>[.<comp4>[.<comp5>...[.<compN>]]]-<incrementState><increment>`

### Examples

| Example     | Major | Minor | Patch | IncrementState | Increment |
|-------------|-------|-------|-------|----------------|-----------|
| 1.2.3       | 1     | 2     | 3     | GA             | 0         |
| 4.5.6-FINAL | 4     | 5     | 6     | GA             | 0         |
| 4.5.6-GA    | 4     | 5     | 6     | GA             | 0         |
| 4.5.6-RC1   | 4     | 5     | 6     | RC             | 1         |
| 4.5.6-DEV3  | 4     | 5     | 6     | DEV            | 3         |
| 1.2.3.4.5.6 | 1     | 2     | 3     | GA             | 4         |

## Creation

Either
```java
SemanticVersion.valueOf("1.2.3")
```

or
```java
SemanticVersion.valueOf(1, 2, 3)
```

## Comparing
```java
semanticVersion1.compareTo(semanticVersion2)
```

with the following outcome

| State                                | Outcome   |
|--------------------------------------|-----------|
| semanticVersion1 < semanticVersion2  | `< 0`     |
| semanticVersion1 == semanticVersion2 | `== 0`    |
| semanticVersion1 > semanticVersion2  | `> 0`     |

## Finding an appropriate version
Imagine the current version is
 * `1.2.3`

and there are updates available
 * `1.2.4`
 * `1.3.0`
 * `2.0.1`
 * `2.0.1-RC1`

the following code will find the appropriate update version matching your desired update strategy:
```java
SemanticVersions.getNewestVersion(updateStrategy, List.of("1.2.4", "1.3.0", "2.0.1", "2.0.2-RC1"), "1.2.3")
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
