// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.completes;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduler;
import org.junit.Test;

import static org.junit.Assert.*;

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
    assertEquals(Integer.valueOf(10), andThenValue);

    completes.with(10);
    final int outcome20 = completes.await();
    assertEquals(20, outcome20);
    assertEquals(Integer.valueOf(20), andThenValue);

    completes.with(20);
    final int outcome40 = completes.await();
    assertEquals(40, outcome40);
    assertEquals(Integer.valueOf(40), andThenValue);
  }

  @Test
  public void testThatCompletesRepeatsForClient() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    Completes<Integer> client = service
            .andThen((value) -> value * 2)
            .andThen((Integer value) -> andThenValue = value)
            .repeat();

    service.with(5);
    final int outcome10 = client.await();
    assertEquals(10, outcome10);
    assertEquals(Integer.valueOf(10), andThenValue);

    service.with(10);
    final int outcome20 = client.await();
    assertEquals(20, outcome20);
    assertEquals(Integer.valueOf(20), andThenValue);
  }
}
