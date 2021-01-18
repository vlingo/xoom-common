// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FutureCompletesTest {
  private Integer andThenValue;
  private Integer failureValue;

  @Test
  public void testCompletesAsTyped() {
    final Completes<Integer> completes = Completes.asTyped();

    completes.with(5);

    completes.await();

    Assert.assertTrue(completes.isCompleted());
    Assert.assertTrue(completes.hasOutcome());
    Assert.assertFalse(completes.hasFailed());
    Assert.assertEquals(5, completes.outcome().intValue());
  }

  @Test
  public void testCompletesWith() {
    final Completes<Integer> completes = Completes.withFailure(5);

    Assert.assertTrue(completes.isCompleted());
    Assert.assertTrue(completes.hasOutcome());
    Assert.assertTrue(completes.hasFailed());
    Assert.assertEquals(new Integer(5), completes.outcome());
  }

  @Test
  public void testNotCompleted() {
    final Completes<Integer> completes = Completes.asInteger();

    Assert.assertFalse(completes.isCompleted());
    Assert.assertFalse(completes.hasOutcome());
    Assert.assertFalse(completes.hasFailed());
    Assert.assertEquals(null, completes.outcome());
  }

  @Test
  public void testCompletesAfterFunction() {
    final Completes<Integer> service = Completes.asInteger();

    final Completes<Integer> client = service.andThen((value) -> value * 2);

    service.with(5);

    client.await();

    Assert.assertEquals(new Integer(10), client.outcome());
  }

  @Test
  public void testCompletesAfterConsumer() {
    final Completes<Integer> service = Completes.asInteger();

    final Completes<Integer> client = service.andThen((value) -> andThenValue = value);

    service.with(5);

    client.await();

    Assert.assertEquals(new Integer(5), client.outcome());
  }

  @Test
  public void testCompletesAfterAndThen() {
    final Completes<Integer> service = Completes.asInteger();

    final Completes<Integer> client =
      service
        .andThen((value) -> value * 2)
        .andThen((value) -> andThenValue = value);

    Assert.assertNotEquals(service, client);

    service.with(5);

    client.await();

    Assert.assertEquals(new Integer(10), andThenValue);
    Assert.assertEquals(new Integer(10), client.outcome());
  }

  @Test
  public void testCompletesAfterAndThenMessageOut() {
    final Completes<Integer> service = Completes.asInteger();

    final Holder holder = new Holder();

    final Completes<Integer> client =
      service
        .andThen((value) -> value * 2)
        .andThen((value) -> { holder.hold(value); return value; } );

    service.with(5);

    client.await();

    Assert.assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testOutcomeBeforeTimeout() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Integer> client =
      service
        .andThen(1000, (value) -> value * 2)
        .andThen((value) -> andThenValue = value);

    service.with(5);

    client.await(10);

    Assert.assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testTimeoutBeforeOutcome() throws Exception {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Integer> client =
      service
        .andThen(1, 0, (value) -> {
          System.out.println("HIT 1");
          return value * 2;
        })
        .andThen((value) -> {
          andThenValue = value;
          System.out.println("HIT 2");
          return value;
        });

    try { Thread.sleep(100); } catch (Exception e) { }

    service.with(5);

    client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertTrue(client.hasOutcome());
    Assert.assertNotEquals(new Integer(10), andThenValue);
    Assert.assertNull(andThenValue);
  }

  @Test
  public void testThatFailureOutcomeFails() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Integer> client =
      service
        .andThen(null, (value) -> value * 2)
        .andThen((Integer value) -> andThenValue = value)
        .otherwise((failedValue) -> failureValue = 1000);

    service.with(null);

    client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertTrue(client.hasOutcome());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(1000), failureValue);
  }

  @Test
  public void testThatFailureOutcomeIsNotExecuted() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Integer> client =
      service
        .andThen(null, (value) -> value * 2)
        .andThen((Integer value) -> andThenValue = value)
        .otherwise((failedValue) -> failureValue = 1000);

    service.with(10);

    client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertEquals(new Integer(20), andThenValue);
    Assert.assertNull(failureValue);
  }

  @Test
  public void testThatNonNullFailureOutcomeFails() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Integer> client =
      service
        .andThen(new Integer(-100), (value) -> 2 * value)
        .andThen((x) -> andThenValue = x)
        .otherwise((x) -> failureValue = 1000);

    service.with(-100);

    final Integer completed = client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertEquals(new Integer(1000), completed);
    Assert.assertEquals(null, andThenValue);
    Assert.assertEquals(new Integer(1000), failureValue);
  }

  @Test
  public void testThatFluentTimeoutWithNonNullFailureTimesout() throws Exception {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Integer> client =
      service
        .useFailedOutcomeOf(new Integer(-100))
        .timeoutWithin(1)
        .andThen(value -> 2 * value)
        .otherwise((Integer failedValue) -> failedValue.intValue() - 100);

    Thread.sleep(100);

    service.with(5);

    final Integer failureOutcome = client.await();

    Assert.assertTrue(service.hasFailed());
    Assert.assertEquals(new Integer(-200), failureOutcome);
  }

  @Test
  public void testThatExceptionOutcomeFails() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Object> client =
      service
        .andThen(null, (value) -> value * 2)
        .andThen((Integer value) -> { throw new IllegalStateException("" + (value * 2)); })
        .recoverFrom((e) -> failureValue = Integer.parseInt(e.getMessage()));

    service.with(2);

    final Integer outcome = client.await();

    Assert.assertNotNull(outcome);
    Assert.assertTrue(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(8, outcome.intValue());
    Assert.assertEquals(8, failureValue.intValue());
  }

  @Test
  public void testThatExceptionOutcomeFailsIfNotRecovered() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Object> client =
            service
                    .andThen(null, (value) -> value * 2)
                    .andThen((Integer value) -> { throw new IllegalStateException("" + (value * 2)); })
                    .recoverFrom((e) -> { throw new IllegalStateException("Not recovered."); });

    service.with(2);

    final Integer outcome = client.await();

    Assert.assertNull(outcome);
    Assert.assertTrue(client.hasFailed());
  }

  @Test
  public void testThatExceptionHandlerDelayRecovers() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Completes<Object> client =
      service
        .andThen(null, (value) -> value * 2)
        .andThen((Integer value) -> { throw new IllegalStateException("" + (value * 2)); })
        .recoverFrom((e) -> {
          failureValue = Integer.parseInt(e.getMessage());
          return failureValue;
        });

    service.with(10);

    client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(40), failureValue);
  }

  @Test
  public void testThatAwaitTimesout() throws Exception {
    final Completes<Integer> service = Completes.using(new Scheduler());

    final Integer completed = service.await(10);

    service.with(5);

    Assert.assertNotEquals(new Integer(5), completed);
    Assert.assertNull(completed);
  }

  @Test
  public void testThatAwaitCompletes() throws Exception {
    final Completes<Integer> completes = Completes.using(new Scheduler());

    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100);
          completes.with(5);
        } catch (Exception e) {
          // ignore
        }
      }
    }.start();

    final Integer completed = completes.await();

    Assert.assertEquals(new Integer(5), completed);
  }

  @Test
  public void testInvertWithFailedOutcome() throws InterruptedException {
    final Outcome<RuntimeException, Completes<String>> failed = Failure.of(new RuntimeException("boom"));
    Completes<Outcome<RuntimeException, String>> inverted = Completes.invert(failed);
    CountDownLatch latch = new CountDownLatch(1);
    inverted.otherwiseConsume(outcome -> {
      Assert.assertTrue("was not Failure", outcome instanceof Failure);
      Assert.assertNull("was not null", outcome.getOrNull());
      Assert.assertEquals("was not the expected error message", "boom", outcome.otherwise(Throwable::getMessage).get());
      latch.countDown();
    });
    Assert.assertTrue("timed out", latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void testInvertWithSuccessOutcomeOfSuccessCompletes() throws InterruptedException {
    final Outcome<RuntimeException, Completes<String>> success = Success.of(Completes.withSuccess("YAY"));
    Completes<Outcome<RuntimeException, String>> inverted = Completes.invert(success);
    CountDownLatch latch = new CountDownLatch(1);
    inverted.andThenConsume(outcome -> {
      Assert.assertTrue("was not Success", outcome instanceof Success);
      Assert.assertNotNull("was null", outcome.getOrNull());
      Assert.assertEquals("was not the expected value", "YAY", outcome.get());
      latch.countDown();
    });
    Assert.assertTrue("timed out", latch.await(1000, TimeUnit.SECONDS));
  }

  @Test
  public void testInvertWithSuccessOutcomeOfFailedCompletes() throws InterruptedException {
    final Outcome<RuntimeException, Completes<String>> successfulFailure = Success.of(Completes.withFailure("ERROR"));
    Completes<Outcome<RuntimeException, String>> inverted = Completes.invert(successfulFailure);
    Assert.assertTrue("hasn't failed", inverted.hasFailed());
    Assert.assertEquals("not equal to ERROR", "ERROR", inverted.outcome().get());
    CountDownLatch latch = new CountDownLatch(1);
    inverted.otherwise((Outcome<RuntimeException,String> outcome) -> { latch.countDown(); return outcome; });
    Assert.assertTrue("timed out", latch.await(1, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testLoneAndThenToCompletes() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    Completes<Integer> client =
      service
        .andThenTo(value -> Completes.withSuccess(value * 2));

    Assert.assertNotEquals(service.id(), client.id());

    final int value = 5;
    service.with(value);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(10), outcome);
  }

  @Test
  public void testAndThenAndThenToCompletes() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    Completes<Integer> client =
      service
        .andThen(value -> value * 2)
        .andThenTo(value -> Completes.withSuccess(value * 2));

    Assert.assertNotEquals(service.id(), client.id());

    final int value = 5;
    service.with(value);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(20), outcome);
  }

  @Test
  public void testAndThenAndThenToAndThenToAndThenCompletes() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    Completes<Integer> client =
      service
        .andThen(value -> value * 2)
        .andThenTo(value -> Completes.withSuccess(value * 2))
        .andThenTo(value -> Completes.withSuccess(value * 2))
        .andThen(value -> value * 2);

    Assert.assertNotEquals(service.id(), client.id());

    final int value = 5;
    service.with(value);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(80), outcome);
  }

  @Test
  public void testNestedCompletesFirst() {
    final Completes<Integer> service = Completes.using(new Scheduler());
    final Completes<Integer> nested = Completes.using(new Scheduler());

    Completes<Integer> client =
            service
                    .andThen(value -> value * 2)
                    .andThenTo(value -> nested.andThen(v -> v * value))
                    .andThenTo(value -> Completes.withSuccess(value * 2))
                    .andThen(value -> value * 2);

    Assert.assertNotEquals(service.id(), client.id());

    nested.with(2);
    service.with(5);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(80), outcome);
  }

  @Test
  public void testNestedCompletesLast() throws InterruptedException {
    final Completes<Integer> service = Completes.using(new Scheduler());
    final Completes<Integer> nested = Completes.using(new Scheduler());

    Completes<Integer> client =
            service
                    .andThen(value -> value * 2)
                    .andThenTo(value -> nested.andThen(v -> v * value))
                    .andThenTo(value -> Completes.withSuccess(value * 2))
                    .andThen(value -> value * 2);

    Assert.assertNotEquals(service.id(), client.id());

    service.with(5);
    Thread.sleep(100);
    nested.with(2);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(new Integer(80), outcome);
  }

  @Test
  public void testOutcomeIsConsumedOncePipelineIsCompleted() throws InterruptedException {
    final Completes<Integer> service = Completes.using(new Scheduler());
    final Completes<Integer> nested = Completes.using(new Scheduler());
    final Holder holder = new Holder();

    Completes<Integer> client =
            service
                    .andThen(value -> value * 2)
                    .andThenTo(value -> nested.andThen(v -> v * value))
                    .andThenTo(value -> Completes.withSuccess(value * 2))
                    .andThenConsume(outcome -> holder.hold(outcome));

    service.with(5);
    Thread.sleep(100);
    nested.with(2);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertEquals(new Integer(40), andThenValue);
    Assert.assertEquals(new Integer(40), outcome);
  }

  @Test
  public void testThatItRecoversFromConsumerException() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    Completes<Integer> client =
            service
                    .andThen(value -> value * 2)
                    .andThenTo(value -> Completes.withSuccess(value * 2))
                    .andThenConsume(outcome -> { throw new RuntimeException(""+(outcome * 2)); })
                    .recoverFrom(e -> Integer.parseInt(e.getMessage()));

    service.with(5);

    final Integer outcome = client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertEquals(new Integer(40), outcome);
  }

  @Test
  public void testAndThenAndThenToAndThenToAndThenCrashCompletes() {
    final Completes<Integer> service = Completes.using(new Scheduler());

    Completes<Integer> client =
      service
        .andThen(value -> value * 2)
        .andThenTo(value -> Completes.withSuccess(value * 2))
        .andThenTo(value -> Completes.withSuccess(value * 2))
        .andThen(value -> multipleBy(value, 2))
        .andThen((Integer value) -> value * 2)
        .recoverFrom(e -> Integer.parseInt(e.getMessage()));

    Assert.assertNotEquals(service.id(), client.id());

    final int value = 5;
    service.with(value);

    final Integer outcome = client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertNull(andThenValue);
    Assert.assertEquals(1000, outcome.intValue());
  }

  @Test
  public void testThatItRecoversConsistentlyWithNoRaceConditions() {
    for (int i = 0; i < 1000; i++) {
      final Completes<Integer> service = Completes.using(new Scheduler());

      Completes<Integer> client =
              service
                      .andThen(value -> value * 2)
                      .andThenTo(value -> Completes.withSuccess(value * 2))
                      .andThenConsume(outcome -> { throw new RuntimeException(""+(outcome * 2)); })
                      .recoverFrom(e -> Integer.parseInt(e.getMessage()));

      service.with(5);

      final Integer outcome = client.await();

      Assert.assertTrue(client.hasFailed());
      Assert.assertEquals(new Integer(40), outcome);
    }
  }

  @Test
  public void testThatFinallyTerminatesThePipeline() {
    final Completes<Integer> service = Completes.asInteger();

    Completes<Integer> client =
            service
                    .andThen(value -> value * 2)
                    .andThenTo(value -> Completes.withSuccess(value * 2))
                    .andFinally();

    service.with(5);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertEquals(new Integer(20), outcome);
  }

  @Test
  public void testThatFinallyRunsOnSuccessfulOutcome() {
    final Completes<Integer> service = Completes.asInteger();

    Completes<Integer> client =
            service
                    .andThen(value -> value * 2)
                    .andThenTo(value -> Completes.withSuccess(value * 2))
                    .andFinally(outcome -> outcome * 3);

    service.with(5);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertEquals(new Integer(60), outcome);
  }

  @Test
  public void testThatItFinallyConsumesTheOutcome() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Completes<Integer> service = Completes.asInteger();

    service
        .andThen(value -> value * 2)
        .andThenTo(value -> Completes.withSuccess(value * 2))
        .andFinallyConsume(outcome -> {
          Assert.assertEquals(new Integer(20), outcome);
          latch.countDown();
        });

    service.with(5);

    Assert.assertTrue("timed out", latch.await(1000, TimeUnit.SECONDS));
  }

  @Test
  public void testItExplicitlyTriggersFailure() {
    final Completes<Integer> service = Completes.asInteger();

    Completes<Integer> client = service
            .useFailedOutcomeOf(-1)
            .andThen(value -> value * 2);

    service.failed();

    final Integer outcome = client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertEquals(new Integer(-1), outcome);
  }

  @Test
  public void testItExplicitlyTriggersFailureWithException() {
    final Completes<Integer> service = Completes.asInteger();

    Completes<Integer> client = service
            .useFailedOutcomeOf(-1)
            .andThen(value -> value * 2);

    service.failed(new RuntimeException());

    final Integer outcome = client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertEquals(null, outcome);
  }

  @Test
  public void testItRecoversFromCompletingWithException() {
    final Completes<Integer> service = Completes.using(new Scheduler());
    final Completes<Integer> client =
            service
                    .andThen((value) -> 2 * value)
                    .recoverFrom((e) -> Integer.valueOf(e.getMessage()));
    service.with(new RuntimeException("10"));

    final Integer outcome = client.await();

    Assert.assertTrue(client.hasFailed());
    Assert.assertEquals(new Integer(10), outcome);
  }

  @Test
  public void testIntermediateStagesReturnTheFinalOutcome() throws InterruptedException {
    final Completes<Integer> service = Completes.using(new Scheduler());
    final Completes<Integer> nested = Completes.using(new Scheduler());

    Completes<Integer> stage1 = service.andThen(value -> value * 2);
    Completes<Integer> stage2 = stage1.andThenTo(value -> nested.andThen(v -> v * value));
    Completes<Integer> stage3 = stage2.andThenTo(value -> Completes.withSuccess(value * 2));
    Completes<Integer> client = stage3.andThen(value -> value * 2);

    service.with(5);
    Thread.sleep(100);
    nested.with(2);

    final Integer outcome = client.await();

    Assert.assertFalse(client.hasFailed());
    Assert.assertEquals(new Integer(80), outcome);
    Assert.assertEquals(new Integer(80), client.outcome());
    Assert.assertEquals(new Integer(80), service.outcome());
    Assert.assertEquals(new Integer(80), stage1.outcome());
    Assert.assertEquals(new Integer(80), stage2.outcome());
    Assert.assertEquals(new Integer(80), stage3.outcome());
    Assert.assertEquals(new Integer(20), nested.outcome());
  }

  private int multipleBy(final int amount, final int by) {
    throw new IllegalStateException("1000");
  }

  private class Holder {
    private void hold(final Integer value) {
      andThenValue = value;
    }
  }
}
