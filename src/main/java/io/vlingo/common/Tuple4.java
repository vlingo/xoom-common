// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

public class Tuple4<A, B, C, D> {
  public final A _1;
  public final B _2;
  public final C _3;
  public final D _4;
  
  public static <A, B, C, D> Tuple4<A, B, C, D> from(final A a, final B b, final C c, final D d) {
    return new Tuple4<A, B, C, D>(a, b, c, d);
  }

  private Tuple4(final A a, final B b, final C c, final D d) {
    this._1 = a;
    this._2 = b;
    this._3 = c;
    this._4 = d;
  }
}
