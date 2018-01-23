// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.fn;

public class Tuple3<A, B, C> {
  public final A _1;
  public final B _2;
  public final C _3;
  
  public static <A, B, C> Tuple3<A, B, C> from(final A a, final B b, final C c) {
    return new Tuple3<A, B, C>(a, b, c);
  }

  private Tuple3(final A a, final B b, final C c) {
    this._1 = a;
    this._2 = b;
    this._3 = c;
  }
}
