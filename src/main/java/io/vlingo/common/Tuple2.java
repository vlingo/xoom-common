// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

public class Tuple2<A, B> {
  public final A _1;
  public final B _2;
  
  public static <A, B> Tuple2<A, B> from(final A a, final B b) {
    return new Tuple2<A, B>(a, b);
  }

  private Tuple2(final A a, final B b) {
    this._1 = a;
    this._2 = b;
  }
}
