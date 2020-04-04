// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

//import io.vlingo.common.BasicCompletes;
//import io.vlingo.common.Completes;
//import io.vlingo.common.Scheduler;
//import org.junit.Test;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static org.junit.Assert.*;

public class SinkAndSourceBasedCompletesTest {
//    private Integer andThenValue;
//    private Integer failureValue;
//
//    @Test
//    public void testCompletesWith() {
//        final Completes<Integer> completes = newCompletesWithOutcome(5);
//        assertEquals(5, completes.outcome().intValue());
//    }
//
//    @Test
//    public void testCompletesAfterFunction() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andFinally((value) -> value * 2);
//
//        completes.with(5);
//        assertEquals(10, completes.outcome().intValue());
//    }
//
//    @Test
//    public void testCompletesAfterConsumer() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen((value) -> andThenValue = value)
//                .andFinally();
//
//        completes.with(5);
//
//        assertEquals(5, completes.outcome().intValue());
//    }
//
//    @Test
//    public void testCompletesAfterAndThen() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen((value) -> value * 2)
//                .andFinally((value) -> andThenValue = value);
//
//        completes.with(5);
//
//        assertEquals(10, completes.outcome().intValue());
//        assertEquals(10, andThenValue.intValue());
//    }
//
//    @Test
//    public void testCompletesAfterAndThenMessageOut() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class);
//
//        completes
//                .andThen((value) -> value * 2)
//                .andFinally((value) -> {
//                    andThenValue = value;
//                    return value;
//                });
//
//        completes.with(5);
//        completes.await();
//
//        assertEquals(10, andThenValue.intValue());
//    }
//
//    @Test
//    public void testAndThenTo() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class);
//        completes.andThenTo(this::newCompletesWithOutcome)
//                .andThen(e -> e * 10)
//                .andFinally();
//
//        completes.with(10);
//
//        assertEquals(100, (int) completes.await());
//    }
//
//    @Test
//    public void testAndThenToWithBasicCompletes() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class);
//        completes.andThenTo(this::newBasicCompletesWithOutcome)
//                .andThen(e -> e * 10)
//                .andFinally();
//
//        completes.with(10);
//
//        assertEquals(100, (int) completes.await());
//    }
//
//    @Test
//    public void testOutcomeBeforeTimeout() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen(1000, (value) -> value * 2)
//                .andFinally((value) -> andThenValue = value);
//
//        completes.with(5);
//        completes.await(10);
//
//        assertEquals(10, andThenValue.intValue());
//    }
//
//    @Test
//    public void testTimeoutBeforeOutcome() throws Exception {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen(1, 0, (value) -> value * 2)
//                .andFinally((value) -> andThenValue = value);
//
//        Thread.sleep(100);
//
//        completes.with(5);
//        completes.await();
//
//        assertTrue(completes.hasFailed());
//        assertNull(andThenValue);
//    }
//
//    @Test
//    public void testThatFailureOutcomeFails() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen(null, (value) -> (Integer) null)
//                .otherwiseConsume((failedValue) -> failureValue = 1000)
//                .andFinally((Integer value) -> andThenValue = value);
//
//        completes.with(null);
//        completes.await();
//
//        assertTrue(completes.hasFailed());
//        assertNull(andThenValue);
//        assertEquals(1000, failureValue.intValue());
//    }
//
//    @Test
//    public void testThatExceptionOutcomeFails() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen(null, (value) -> value * 2)
//                .andThenConsume((Integer value) -> {
//                  throw new IllegalStateException("" + (value * 2));
//                })
//                .recoverFrom((e) -> {
//                    failureValue = Integer.parseInt(e.getMessage());
//                    throw new IllegalStateException(e);
//                });
//
//        completes.with(2);
//        completes.await();
//
//        assertTrue(completes.hasFailed());
//        assertNull(andThenValue);
//        assertEquals(8, failureValue.intValue());
//    }
//
//    @Test
//    public void testThatAwaitTimesOut() throws Exception {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class);
//        final Integer completed = completes.await(10);
//
//        completes.with(5);
//
//        assertNull(completed);
//    }
//
//    @Test
//    public void testThatAwaitCompletes() throws Exception {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class);
//
//        new Thread(() -> {
//            try {
//                Thread.sleep(100);
//                completes.with(5);
//            } catch (Exception e) {
//                // ignore
//            }
//        }).start();
//
//        final Integer completed = completes.await();
//
//        assertEquals(5, completed.intValue());
//    }
//
//    @Test
//    public void testThatCompletesRepeats() {
//        final Completes<Integer> completes = newEmptyCompletes(Integer.class)
//                .andThen((value) -> value * 2)
//                .andThen((Integer value) -> andThenValue = value)
//                .repeat();
//
//        completes.andFinallyConsume(e -> {
//        });
//
//        completes.with(5);
//        assertEquals(10, andThenValue.intValue());
//        completes.with(10);
//        assertEquals(20, andThenValue.intValue());
//        completes.with(20);
//        assertEquals(40, andThenValue.intValue());
//    }
//
//    @Test
//    public void testOnClientAndServerSetupWhenClientIsFaster() throws InterruptedException {
//        List<Integer> ints = new LinkedList<>();
//        List<Integer> expected = IntStream.range(0, 10000).boxed().collect(Collectors.toList());
//        Completes<Object> completeInteger = newEmptyCompletes(Integer.class)
//            .andThenConsume(ints::add)
//            .andFinally()
//            .repeat();
//
//        Thread server = new Thread(() -> expected.forEach(completeInteger::with));
//
//        server.start();
//        server.join();
//
//        HashSet<Integer> intHashSet = new HashSet<>(ints);
//        HashSet<Integer> expectedHashSet = new HashSet<>(expected);
//
//        expectedHashSet.removeAll(intHashSet);
//        assertEquals("Completes was: " + completeInteger.toString() + " | HashSet was: " + expectedHashSet.toString(), 0, expectedHashSet.size());
//    }
//
//    @Test
//    public void testOnClientAndServerSetupWhenServerIsFaster() throws InterruptedException {
//        List<Integer> ints = new LinkedList<>();
//        List<Integer> expected = IntStream.range(0, 10000).boxed().collect(Collectors.toList());
//        Completes<Integer> completeInteger = newEmptyCompletes(Integer.class);
//
//        Thread server = new Thread(() -> expected.forEach(completeInteger::with));
//        Thread client = new Thread(() -> completeInteger
//                .andThenConsume(ints::add)
//                .andFinally()
//                .repeat());
//
//        server.start();
//        Thread.sleep(10);
//        client.start();
//        server.join();
//        client.join();
//
//        HashSet<Integer> intHashSet = new HashSet<>(ints);
//        HashSet<Integer> expectedHashSet = new HashSet<>(expected);
//
//        expectedHashSet.removeAll(intHashSet);
//        assertEquals("Completes was: " + completeInteger.toString() + " | HashSet was: " + expectedHashSet.toString(), 0, expectedHashSet.size());
//    }
//
//    private <T> Completes<T> newCompletesWithOutcome(T outcome) {
//        SinkAndSourceBasedCompletes<T> completes = SinkAndSourceBasedCompletes.withScheduler(new Scheduler());
//        completes.with(outcome);
//
//        return completes;
//    }
//
//    private <T> Completes<T> newEmptyCompletes(Class<T> _class) {
//        return SinkAndSourceBasedCompletes.withScheduler(new Scheduler());
//    }
//
//    private <O> Completes<O> newBasicCompletesWithOutcome(O data) {
//        return new BasicCompletes<>(new Scheduler()).with(data);
//    }
}
