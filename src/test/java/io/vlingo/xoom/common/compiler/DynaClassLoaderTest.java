// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.compiler;

import static io.vlingo.xoom.common.compiler.DynaFile.GeneratedTestSources;
import static io.vlingo.xoom.common.compiler.DynaFile.toFullPath;
import static io.vlingo.xoom.common.compiler.DynaFile.toPackagePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import io.vlingo.xoom.common.compiler.DynaCompiler.Input;

public class DynaClassLoaderTest extends DynaTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testDynaClassLoader() throws Exception {
    final DynaClassLoader classLoader = new DynaClassLoader(ClassLoader.getSystemClassLoader());
    
    // load a class from the default/parent ClassLoader
    final Class<ByteCode> byteCodeClass = (Class<ByteCode>) classLoader.loadClass("io.vlingo.xoom.common.compiler.ByteCode");
    assertNotNull(byteCodeClass);
    
    final String relativeTargetFile = toFullPath(classname);
    final String pathToGeneratedSource = toPackagePath(classname);
    new File(GeneratedTestSources + pathToGeneratedSource).mkdirs();
    final String pathToSource = GeneratedTestSources + relativeTargetFile + ".java";

    final Input input =
            new Input(
                    TestInterface.class,
                    classname,
                    source,
                    DynaFile.persistDynaClassSource(pathToSource, source),
                    classLoader,
                    DynaType.Test,
                    false);
    
    new DynaCompiler().compile(input);
    
    // load a brand new class just added to the DynaClassLoader
    final Class<TestInterface> testClass = (Class<TestInterface>) classLoader.loadClass(classname);
    
    assertNotNull(testClass);
    assertNotNull(testClass.newInstance());
    assertEquals(1, testClass.newInstance().test());
    
    // load another class from the default/parent ClassLoader
    final Class<DynaFile> actorDynaClass = (Class<DynaFile>) classLoader.loadClass("io.vlingo.xoom.common.compiler.DynaFile");
    assertNotNull(actorDynaClass);
  }
  
  public static interface TestInterface {
    int test();
  }
}
