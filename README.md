# vlingo-common

[![Javadocs](http://javadoc.io/badge/io.vlingo/vlingo-common.svg?color=brightgreen)](http://javadoc.io/doc/io.vlingo/vlingo-common) [![Build](https://github.com/vlingo/vlingo-common/workflows/Build/badge.svg)](https://github.com/vlingo/vlingo-common/actions?query=workflow%3ABuild) [ ![Download](https://api.bintray.com/packages/vlingo/vlingo-platform-java/vlingo-common/images/download.svg) ](https://bintray.com/vlingo/vlingo-platform-java/vlingo-common/_latestVersion) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/community)

The VLINGO XOOM platform SDK common tools shared across various projects.

Docs: https://docs.vlingo.io/vlingo-common

The `vlingo-common` project consists of a minimal toolset used across various other VLINGO XOOM SDK components.

  - `Completes<T>` and `FutureCompletes`
  - `IdentityGenerator` with various identity generation algorithms
  - `Outcome` with `Success` and `Failure` (like `Either` and `Try`)
  - `Scheduler`
  - Serialization
  - `SemanticVersion`
  - `DynaCompiler` (makes `JavaCompiler` simpiler to use)
  - `Tuple2`, `Tuple3`, `Tuple4`, `Tuple5`

### Important
If using snapshot builds [follow these instructions](https://github.com/vlingo/vlingo-platform#snapshots-repository) or you will experience failures.

### Bintray

```xml
  <repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
  </repositories>
  <dependency>
    <groupId>io.vlingo</groupId>
    <artifactId>vlingo-common</artifactId>
    <version>1.5.0</version>
    <type>pom</type>
  </dependency>
```

```gradle
dependencies {
    compile 'io.vlingo:vlingo-common:1.5.0'
}

repositories {
    jcenter()
}
```


License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2020 VLINGO LABS. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.
