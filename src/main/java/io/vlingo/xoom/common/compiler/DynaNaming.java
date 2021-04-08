// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.compiler;

public class DynaNaming {
  public static String classnameFor(final Class<?> protocolInterface, final String postfix) {
    final StringBuilder builder = new StringBuilder();
    
    Class<?> declaringClass = protocolInterface.getDeclaringClass();
    while (declaringClass != null) {
      builder.insert(0, declaringClass.getSimpleName());
      declaringClass = declaringClass.getDeclaringClass();
    }
    
    builder.append(protocolInterface.getSimpleName()).append(postfix);
    
    return builder.toString();
  }
  
  public static String fullyQualifiedClassnameFor(final Class<?> protocolInterface, final String postfix) {
    final StringBuilder builder = new StringBuilder();
    
    builder
      .append(protocolInterface.getPackage().getName())
      .append(".")
      .append(classnameFor(protocolInterface, postfix));
    
    return builder.toString();
  }
}
