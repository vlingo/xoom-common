// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import org.junit.Assert;
import org.junit.Test;

public class TupleTest {
  private int count = 0;

  @Test
  public void testThatTuple2Iterates() {
    final Tuple2<Integer, Long> tuple = Tuple2.tuple(2, 2L);
    tuple.forEach((Number value) -> { ++count; Double d = value.doubleValue(); Assert.assertEquals(2.0d, d, 0); });
    Assert.assertEquals(2, count);
  }

  @Test
  public void testThatTuple2Maps() {
    final Tuple2<Integer, Long> tuple = Tuple2.tuple(2, 2L);
    tuple.map((Number value) -> String.valueOf(value)).forEach(value ->  { ++count; Assert.assertEquals("2", value); } );
    Assert.assertEquals(2, count);
  }

  @Test
  public void testThatTuple3Iterates() {
    final Tuple3<Integer, Long, Short> tuple = Tuple3.tuple(2, 2L, (short) 2);
    tuple.forEach((Number value) -> { ++count; Double d = value.doubleValue(); Assert.assertEquals(2.0d, d, 0); });
    Assert.assertEquals(3, count);
  }

  @Test
  public void testThatTuple3Maps() {
    final Tuple3<Integer, Long, Short> tuple = Tuple3.tuple(2, 2L, (short) 2);
    tuple.map((Number value) -> String.valueOf(value)).forEach(value -> { ++count; Assert.assertEquals("2", value); });
    Assert.assertEquals(3, count);
  }

  @Test
  public void testThatTuple4Iterates() {
    final Tuple4<Integer, Long, Short, Byte> tuple = Tuple4.tuple(2, 2L, (short) 2, (byte) 2);
    tuple.forEach((Number value) -> { ++count; Double d = value.doubleValue(); Assert.assertEquals(2.0d, d, 0); });
    Assert.assertEquals(4, count);
  }

  @Test
  public void testThatTuple4Maps() {
    final Tuple4<Integer, Long, Short, Byte> tuple = Tuple4.tuple(2, 2L, (short) 2, (byte) 2);
    tuple.map((Number value) -> String.valueOf(value)).forEach(value -> { ++count; Assert.assertEquals("2", value); });
    Assert.assertEquals(4, count);
  }

  @Test
  public void testThatTuple5Iterates() {
    final Tuple5<Integer, Long, Short, Byte, Float> tuple = Tuple5.tuple(2, 2L, (short) 2, (byte) 2, 2.0f);
    tuple.forEach((Number value) -> { ++count; Double d = value.doubleValue(); Assert.assertEquals(2.0d, d, 0); });
    Assert.assertEquals(5, count);
  }

  @Test
  public void testThatTuple5Maps() {
    final Tuple5<Integer, Long, Short, Byte, Float> tuple = Tuple5.tuple(2, 2L, (short) 2, (byte) 2, 2.0f);
    tuple.map((Number value) -> String.valueOf(value.intValue())).forEach(value -> { ++count; Assert.assertEquals("2", value); });
    Assert.assertEquals(5, count);
  }
}
