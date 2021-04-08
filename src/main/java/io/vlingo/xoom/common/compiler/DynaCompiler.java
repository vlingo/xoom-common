// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.compiler;

import static io.vlingo.xoom.common.compiler.DynaFile.RootOfMainClasses;
import static io.vlingo.xoom.common.compiler.DynaFile.RootOfTestClasses;
import static io.vlingo.xoom.common.compiler.DynaFile.persistDynaClass;
import static io.vlingo.xoom.common.compiler.DynaFile.toFullPath;
import static io.vlingo.xoom.common.compiler.DynaFile.toPackagePath;

import java.io.File;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class DynaCompiler {
  public static class Input {
    public final DynaClassLoader classLoader;
    public final String fullyQualifiedClassname;
    public final boolean persist;
    public final Class<?> protocol;
    public final DynaType type;
    public final String source;
    public final File sourceFile;
    
    public <T> Input(
            final Class<T> protocol,
            final String fullyQualifiedClassname,
            final String source,
            final File sourceFile,
            final DynaClassLoader classLoader,
            final DynaType type,
            final boolean persist) {
      this.protocol = protocol;
      this.fullyQualifiedClassname = fullyQualifiedClassname;
      this.source = source;
      this.sourceFile = sourceFile;
      this.classLoader = classLoader;
      this.type = type;
      this.persist = persist;
    }
  }
  
  private final JavaCompiler compiler;
  
  public DynaCompiler() {
    this.compiler = ToolProvider.getSystemJavaCompiler();
  }

  public <T> Class<T> compile(final Input input) throws Exception {
    final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    final DiagnosticListener<JavaFileObject> listener = new DynaDiagnosticListener<>();
    
    try (final DynaFileManager dynaFileManager = new DynaFileManager(input.protocol, fileManager, input.classLoader)) {
      final Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(input.sourceFile));
      final CompilationTask task = compiler.getTask(null, dynaFileManager, listener, null, null, sources);
      if (task.call()) {
        persist(input, dynaFileManager.byteCode);
        return input.classLoader.addDynaClass(input.fullyQualifiedClassname, dynaFileManager.byteCode);
      }
    } catch (Exception e) {
      System.out.println("Dynamically generated class source for " + input.fullyQualifiedClassname + " did not compile because: " + e.getMessage());
      e.printStackTrace();
      // fall through
    }
    
    throw new IllegalArgumentException("Dynamically generated class source did not compile: " + input.fullyQualifiedClassname);
  }
  
  private File persist(final Input input, final ByteCode byteCode) throws Exception {
    final String relativePathToClass = toFullPath(input.fullyQualifiedClassname);
    final String pathToCompiledClass = toPackagePath(input.fullyQualifiedClassname);
    final String rootOfGenerated = input.type == DynaType.Main ? RootOfMainClasses : RootOfTestClasses;
    new File(rootOfGenerated + pathToCompiledClass).mkdirs();
    final String pathToClass = rootOfGenerated + relativePathToClass + ".class";
    
    return input.persist ? persistDynaClass(pathToClass, byteCode.bytes()) : new File(relativePathToClass);
  }

  private static class DynaDiagnosticListener<T extends JavaFileObject> implements DiagnosticListener<T> {
    @Override
    public void report(final Diagnostic<? extends T> diagnostic) {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        // TODO: log
        System.out.println("vlingo/common: DynaCompiler ERROR: " + diagnostic);
      }
    }
  }
}
