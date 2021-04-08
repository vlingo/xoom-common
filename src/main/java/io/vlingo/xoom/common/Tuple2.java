// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Tuple2<A, B> {
  public final A _1;
  public final B _2;

  public static <A, B> Tuple2<A, B> from(final A a, final B b) {
    return new Tuple2<A, B>(a, b);
  }

  public static <A, B> Tuple2<A, B> tuple(final A a, final B b) {
    return new Tuple2<A, B>(a, b);
  }

  @SuppressWarnings("unchecked")
  <T> void forEach(final Consumer<? super T> consumer) {
    consumer.accept((T) _1);
    consumer.accept((T) _2);
  }

  @SuppressWarnings("unchecked")
  <TA,TB> Collection<TB> map(final Function<? super TA, ? super TB> function) {
    final List<TB> list = new ArrayList<>(2);

    list.add((TB) function.apply((TA) _1));
    list.add((TB) function.apply((TA) _2));

    return list;
  }

  private Tuple2(final A a, final B b) {
    this._1 = a;
    this._2 = b;
  }

  @Override
  public String toString() {
    return "Tuple2 [_1=" + _1 + ", _2=" + _2 + "]";
  }
}
