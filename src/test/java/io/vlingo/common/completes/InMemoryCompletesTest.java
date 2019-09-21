// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;
import org.junit.Test;

import static org.junit.Assert.*;

public class InMemoryCompletesTest {
    private Integer andThenValue;
    private Integer failureValue;

    @Test
    public void testCompletesWith() {
        final Completes<Integer> completes = newCompletesWithOutcome(5).ready();
        assertEquals(5, completes.outcome().intValue());
    }

    @Test
    public void testCompletesAfterFunction() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen((value) -> value * 2)
                .ready();

        completes.with(5);
        assertEquals(10, completes.outcome().intValue());
    }

    @Test
    public void testCompletesAfterConsumer() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen((value) -> andThenValue = value)
                .ready();

        completes.with(5);

        assertEquals(5, completes.outcome().intValue());
    }

    @Test
    public void testCompletesAfterAndThen() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen((value) -> value * 2)
                .andThen((value) -> andThenValue = value)
                .ready();

        completes.with(5);

        assertEquals(10, andThenValue.intValue());
        assertEquals(10, completes.outcome().intValue());
    }

    @Test
    public void testCompletesAfterAndThenMessageOut() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class);

        final Holder holder = new Holder();

        completes
                .andThen((value) -> value * 2)
                .andThen((value) -> {
                    holder.hold(value);
                    return value;
                });

        completes.with(5);
        completes.await();

        assertEquals(10, andThenValue.intValue());
    }

    @Test
    public void testOutcomeBeforeTimeout() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen(1000, (value) -> value * 2)
                .andThen((value) -> andThenValue = value);

        completes.with(5);
        completes.await(10);

        assertEquals(10, andThenValue.intValue());
    }

    @Test
    public void testTimeoutBeforeOutcome() throws Exception {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen(1, 0, (value) -> value * 2)
                .andThen((value) -> andThenValue = value);

        Thread.sleep(100);

        completes.with(5);
        completes.await();

        assertTrue(completes.hasFailed());
        assertNull(andThenValue);
    }

    @Test
    public void testThatFailureOutcomeFails() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen(null, (value) -> (Integer) null)
                .andThen((Integer value) -> andThenValue = value)
                .otherwiseConsume((failedValue) -> failureValue = 1000)
                .ready();

        completes.with(null);

        assertTrue(completes.hasFailed());
        assertNull(andThenValue);
        assertEquals(1000, failureValue.intValue());
    }

    @Test
    public void testThatExceptionOutcomeFails() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen(null, (value) -> value * 2)
                .andThenConsume((Integer value) -> {
                    throw new IllegalStateException("" + (value * 2));
                })
                .recoverFrom((e) -> {
                    failureValue = Integer.parseInt(e.getMessage());
                    throw new IllegalStateException(e);
                });

        completes.with(2);
        completes.await();

        assertTrue(completes.hasFailed());
        assertNull(andThenValue);
        assertEquals(8, failureValue.intValue());
    }

    @Test
    public void testThatAwaitTimesOut() throws Exception {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class);
        final Integer completed = completes.await(10);

        completes.with(5);

        assertNull(completed);
    }

    @Test
    public void testThatAwaitCompletes() throws Exception {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class);

        new Thread(() -> {
            try {
                Thread.sleep(100);
                completes.with(5);
            } catch (Exception e) {
                // ignore
            }
        }).start();

        final Integer completed = completes.await();

        assertEquals(5, completed.intValue());
    }

    @Test
    public void testThatCompletesRepeats() {
        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
                .andThen((value) -> value * 2)
                .andThen((Integer value) -> andThenValue = value)
                .repeat()
                .ready();

        completes.with(5);
        assertEquals(10, andThenValue.intValue());
        completes.with(10);
        assertEquals(20, andThenValue.intValue());
        completes.with(20);
        assertEquals(40, andThenValue.intValue());
    }

    private class Holder {
        private void hold(final Integer value) {
            andThenValue = value;
        }
    }

    private <T> Completes<T> newCompletesWithOutcome(T outcome) {
        InMemoryCompletes<T> completes = InMemoryCompletes.withScheduler(new Scheduler());
        completes.with(outcome);

        return completes;
    }

    private <T> Completes<T> newEmptyCompletes(Class<T> _class) {
        return InMemoryCompletes.withScheduler(new Scheduler());
    }
}
