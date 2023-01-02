// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.compiler;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProxyFileTest extends DynaTest {
  private String parentPath;
  private File parentPathFile;
  private String pathToJavaSource;
  private File pathToJavaSourceFile;
  
  @Test
  public void testPersistProxyClassSource() throws Exception {
    parentPathFile.mkdirs();
    DynaFile.persistDynaClassSource(pathToJavaSource, source);
    final InputStream input = new FileInputStream(pathToJavaSourceFile);
    final byte[] sourceBytes = new byte[source.getBytes().length];
    input.read(sourceBytes);
    input.close();
    assertEquals(source, new String(sourceBytes));
  }

  @Test
  public void testToFullPath() {
    final String path = DynaFile.toFullPath("io.vlingo.xoom.actors.Startable");
    assertEquals("io/vlingo/xoom/actors/Startable", path);
  }

  @Test
  public void testToPackagePath() {
    final String path = DynaFile.toPackagePath("io.vlingo.xoom.actors.Startable");
    assertEquals("io/vlingo/xoom/actors", path);
  }
  
  @Before
  public void setUp() {
    parentPath = System.getProperty("java.io.tmpdir") + "/" + DynaFile.toPackagePath(classname);
    parentPathFile = new File(parentPath);
    pathToJavaSource = System.getProperty("java.io.tmpdir") + "/" + DynaFile.toFullPath(classname) + ".java";
    pathToJavaSourceFile = new File(pathToJavaSource);
  }
  
  @After
  public void tearDown() {
    pathToJavaSourceFile.delete();
    parentPathFile.delete();
  }
}
