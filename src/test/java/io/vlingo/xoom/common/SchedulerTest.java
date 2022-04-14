// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchedulerTest {
  private Scheduled<CounterHolder> scheduled;
  private Scheduler scheduler;
  
  @Test
  public void testScheduleOnceOneHappyDelivery() throws Exception {
    final CounterHolder holder = new CounterHolder(1);
    
    scheduler.scheduleOnce(scheduled, holder, 0L, 1L);
    
    holder.completes();
    
    assertEquals(1, holder.counter);
  }
  
  @Test
  public void testScheduleManyHappyDelivery() throws Exception {
    final CounterHolder holder = new CounterHolder(505);
    
    scheduler.schedule(scheduled, holder, 0L, 1L);
    
    holder.completes();
    
    assertFalse(0 == holder.counter);
    assertFalse(1 == holder.counter);
    assertTrue(holder.counter > 500);
  }
  
  @Before
  public void setUp() {
    scheduled = new Scheduled<CounterHolder>() {
      @Override
      public void intervalSignal(final Scheduled<CounterHolder> scheduled, final CounterHolder data) {
        data.increment();
      }
    };
    
    scheduler = new Scheduler();
  }
  
  @After
  public void tearDown() {
    scheduler.close();
  }
  
  public static class CounterHolder {
    public int counter;
    public CountDownLatch until;
    
    public CounterHolder(final int totalExpected) {
      this.until = new CountDownLatch(totalExpected);
    }

    public void completes() {
      try {
        until.await();
      } catch (Exception e) {
        // ignore
      }
    }

    public void increment() {
      ++counter;
      until.countDown();
    }
  }
}
