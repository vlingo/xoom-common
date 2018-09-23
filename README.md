# vlingo-common

[![Build Status](https://travis-ci.org/vlingo/vlingo-common.svg?branch=master)](https://travis-ci.org/vlingo/vlingo-common) [ ![Download](https://api.bintray.com/packages/vlingo/vlingo-platform-java/vlingo-common/images/download.svg) ](https://bintray.com/vlingo/vlingo-platform-java/vlingo-common/_latestVersion)

This is a very early stage release of the vlingo platform.

The vlingo-common project consists of some tools that are used across
various other vlingo projects.

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
    <version>0.7.0</version>
    <type>pom</type>
  </dependency>
```

```gradle
dependencies {
    compile 'io.vlingo:vlingo-common:0.7.0'
}

repositories {
    jcenter()
}
```

License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2018 Vaughn Vernon. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.
