// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

/**
 * Holds any type of value and provides dynamic casting to
 * its direct type and any of its super types.
 */
public class Union {
  private final Object value;

  /**
   * Answer a new {@code Union} that holds {@code value}.
   * @param value the Object to be held by the Union
   * @return Union
   */
  public static Union with(final Object value) {
    return new Union(value);
  }

  /**
   * Answer whether or not my {@code value}'s type is equal to {@code type}.
   * @param type the Class to compare to my value's type
   * @return boolean
   */
  public boolean is(final Class<?> type) {
    return (value.getClass() == type);
  }

  /**
   * Answer the {@code Class} type of my {@code value}.
   * @return {@code Class<?>}
   */
  public Class<?> type() {
    return value.getClass();
  }

  /**
   * Answer my {@code value} as a {@code T}. If my {@code value} is
   * not of the expect type a {@code ClassCastException} will be thrown.
   * @param <T> the type to answer
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T> T value() {
    return (T) value;
  }

  /**
   * Construct my state.
   * @param value the Object to set as my value
   */
  private Union(final Object value) {
    this.value = value;
  }
}
