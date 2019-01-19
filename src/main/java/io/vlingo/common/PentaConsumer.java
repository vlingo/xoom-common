// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import java.util.Objects;
import java.util.function.Consumer;
/**
 * Represents an operation that accepts five input arguments and returns no
 * result.  This is the five-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code PentaConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <V> the type of the third argument to the operation
 * @param <W> the type of the fourth argument to the operation
 * @param <X> the type of the fifth argument to the operation
 *
 * @see Consumer
 */
@FunctionalInterface
public interface PentaConsumer<T, U, V, W, X> {
  
  /**
   * Performs this operation on the given arguments.
   *
   * @param t the first input argument
   * @param u the second input argument
   * @param v the third input argument
   * @param w the fourth input argument
   * @param x the fifth input argument
   */
  void accept(T t, U u, V v, W w, X x);

  /**
   * Returns a composed {@code PentaConsumer} that performs, in sequence, this
   * operation followed by the {@code after} operation. If performing either
   * operation throws an exception, it is relayed to the caller of the
   * composed operation.  If performing this operation throws an exception,
   * the {@code after} operation will not be performed.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code PentaConsumer} that performs in sequence this
   * operation followed by the {@code after} operation
   * @throws NullPointerException if {@code after} is null
   */
  default PentaConsumer<T, U, V, W, X> andThen(PentaConsumer<? super T, ? super U, ? super V, ? super W, ? super X> after) {
      Objects.requireNonNull(after);

      return (a, b, c, d, e) -> {
          accept(a, b, c, d, e);
          after.accept(a, b, c, d, e);
      };
  }
}
