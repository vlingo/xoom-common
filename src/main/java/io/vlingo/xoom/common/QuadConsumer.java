// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import java.util.Objects;
import java.util.function.Consumer;
/**
 * Represents an operation that accepts four input arguments and returns no
 * result.  This is the four-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code QuadConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <V> the type of the third argument to the operation
 * @param <W> the type of the fourth argument to the operation
 *
 * @see Consumer
 */
@FunctionalInterface
public interface QuadConsumer<T, U, V, W> {
  
  /**
   * Performs this operation on the given arguments.
   *
   * @param t the first input argument
   * @param u the second input argument
   * @param v the third input argument
   * @param w the fourth input argument
   */
  void accept(T t, U u, V v, W w);

  /**
   * Returns a composed {@code QuadConsumer} that performs, in sequence, this
   * operation followed by the {@code after} operation. If performing either
   * operation throws an exception, it is relayed to the caller of the
   * composed operation.  If performing this operation throws an exception,
   * the {@code after} operation will not be performed.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code QuadConsumer} that performs in sequence this
   * operation followed by the {@code after} operation
   * @throws NullPointerException if {@code after} is null
   */
  default QuadConsumer<T, U, V, W> andThen(QuadConsumer<? super T, ? super U, ? super V, ? super W> after) {
      Objects.requireNonNull(after);

      return (a, b, c, d) -> {
          accept(a, b, c, d);
          after.accept(a, b, c, d);
      };
  }
}
