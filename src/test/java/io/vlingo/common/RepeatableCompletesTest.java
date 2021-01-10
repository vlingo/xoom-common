// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RepeatableCompletesTest {
  private Integer andThenValue;

  @Test
  public void testThatCompletesRepeats() {
    final Completes<Integer> completes = Completes.asTyped();

    completes
      .andThen((value) -> value * 2)
      .andThen((Integer value) -> andThenValue = value)
      .repeat();

    completes.with(5);
    final int outcome10 = completes.await();
    assertEquals(10, outcome10);
    assertEquals(new Integer(10), andThenValue);

    completes.with(10);
    final int outcome20 = completes.await();
    assertEquals(20, outcome20);
    assertEquals(new Integer(20), andThenValue);

    completes.with(20);
    final int outcome40 = completes.await();
    assertEquals(40, outcome40);
    assertEquals(new Integer(40), andThenValue);
  }

  @Test
  public void testThatCompletesRepeatsAfterFailure() {
    final Completes<Integer> completes = Completes.asTyped();

    completes
            .andThen((value) -> {
              if (value < 10) throw new RuntimeException();
              return value;
            })
            .andThen((value) -> value * 2)
            .andThen((Integer value) -> andThenValue = value)
            .repeat();

    completes.with(5);
    completes.await();
    assertTrue(completes.hasFailed());

    completes.with(10);
    final int outcome20 = completes.await();
    assertEquals(20, outcome20);
    assertEquals(new Integer(20), andThenValue);
  }


  @Test
  public void testThatCompletesRepeatsAfterTimeout() {
    final Completes<Integer> completes = Completes.using(new Scheduler());

    completes
            .andThen(1, (value) -> value * 2)
            .andThen((Integer value) -> andThenValue = value)
            .repeat();

    try { Thread.sleep(100); } catch (Exception e) { }

    completes.with(5);
    completes.await(10);
    assertTrue(completes.hasFailed());

    completes.with(10);
    final int outcome20 = completes.await();
    assertEquals(20, outcome20);
    assertEquals(new Integer(20), andThenValue);
  }
}
