// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.common;

import java.util.Objects;
import java.util.function.Function;
/**
 * Represents a function that accepts four arguments and produces a result.
 * This is the four-arity specialization of {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <W> the type of the fourth argument to the function
 * @param <R> the type of the result of the function
 *
 * @see Function
 */
@FunctionalInterface
public interface QuadFunction<T, U, V, W, R> {
  
  /**
   * Applies this function to the given arguments.
   *
   * @param t the first function argument
   * @param u the second function argument
   * @param v the third function argument
   * @param w the third function argument
   * 
   * @return the function result
   */
  R apply(T t, U u, V v, W w);

  /**
   * Returns a composed function that first applies this function to
   * its input, and then applies the {@code after} function to the result.
   * If evaluation of either function throws an exception, it is relayed to
   * the caller of the composed function.
   *
   * @param <R2> the type of output of the {@code after} function, and of the
   *           composed function
   * @param after the function to apply after this function is applied
   * 
   * @return a composed function that first applies this function and then
   * applies the {@code after} function
   * 
   * @throws NullPointerException if after is null
   */
  default <R2> QuadFunction<T, U, V, W, R2> andThen(Function<? super R, ? extends R2> after) {
    Objects.requireNonNull(after);
    return (T t, U u, V v, W w) -> after.apply(apply(t, u, v, w));
  }
}