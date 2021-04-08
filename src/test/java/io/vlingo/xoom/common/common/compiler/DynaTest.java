// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.compiler;

public abstract class DynaTest {
  public final String classname = "io.vlingo.xoom.common.compiler.TestProxy";
  
  public final String source = "package io.vlingo.xoom.common.compiler; public class TestProxy implements io.vlingo.xoom.common.compiler.DynaClassLoaderTest.TestInterface { public int test() { return 1; } }";
}
