# xoom-common

[![Javadocs](http://javadoc.io/badge/io.vlingo.xoom/xoom-common.svg?color=brightgreen)](http://javadoc.io/doc/io.vlingo.xoom/xoom-common) [![Build](https://github.com/vlingo/xoom-common/workflows/Build/badge.svg)](https://github.com/vlingo/xoom-common/actions?query=workflow%3ABuild) [![Download](https://img.shields.io/maven-central/v/io.vlingo.xoom/xoom-common?label=maven)](https://search.maven.org/artifact/io.vlingo.xoom/xoom-common) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/community)

The VLINGO XOOM platform SDK common tools shared across various projects.

Docs: https://docs.vlingo.io/xoom-common

The `xoom-common` project consists of a minimal toolset used across various other VLINGO XOOM SDK components.

  - `Completes<T>` and `FutureCompletes`
  - `IdentityGenerator` with various identity generation algorithms
  - `Outcome` with `Success` and `Failure` (like `Either` and `Try`)
  - `Scheduler`
  - Serialization
  - `SemanticVersion`
  - `DynaCompiler` (makes `JavaCompiler` simpiler to use)
  - `Tuple2`, `Tuple3`, `Tuple4`, `Tuple5`

### Installation

```xml
  <dependency>
    <groupId>io.vlingo.xoom</groupId>
    <artifactId>xoom-common</artifactId>
    <version>1.11.0</version>
    <type>pom</type>
  </dependency>
```

```gradle
dependencies {
    compile 'io.vlingo.xoom:xoom-common:1.11.0'
}
```


License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2022 VLINGO LABS. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.
