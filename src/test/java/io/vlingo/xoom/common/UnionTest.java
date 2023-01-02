// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import org.junit.Assert;
import org.junit.Test;

public class UnionTest {

  @Test
  public void testThatUnionProvidesExpectedValue() {
    final Union union = Union.with(42);
    Assert.assertTrue(union.is(Integer.class));
    Assert.assertEquals(Integer.class, union.type());
    Assert.assertEquals(42, (int) union.value());
  }

  @Test(expected = ClassCastException.class)
  public void testThatUnionThrowsOnUnexpectedType() {
    final Union union = Union.with(42);
    Assert.assertTrue(union.is(Integer.class));
    Assert.assertEquals(Integer.class, union.type());
    Assert.assertFalse(union.is(String.class));
    Assert.assertNotEquals(String.class, union.type());
    final String badCast = union.value();
    Assert.assertEquals("", badCast); // not reached
  }
}
