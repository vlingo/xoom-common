// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class OutcomeTest {
    @Test
    public void testThatASuccessfulOutcomeCanBeResolved() {
        final int initialValue = randomInteger();
        final int failedValue = randomInteger();
        final int successInt = randomInteger();
        final int expected = successInt * initialValue;

        final int outcome = Success.of(initialValue).resolve(ex -> failedValue, value -> value * successInt);
        assertEquals(expected, outcome);
    }

    @Test
    public void testThatASuccessfulOutcomeCanBeComposed() throws Throwable {
        final int initialValue = randomInteger();
        final int successInt = randomInteger();
        final int expected = initialValue * successInt;

        final int outcome = Success.of(initialValue).andThen(value -> value * successInt).get();
        assertEquals(expected, outcome);
    }

    @Test
    public void testThatASuccessfulOutcomeCanBeComposedWithANewOutcome() throws Throwable {
        final int initialValue = randomInteger();
        final int successInt = randomInteger();
        final int expected = initialValue * successInt;

        final int outcome = Success.of(initialValue).andThenTo(value -> Success.of(value * successInt)).get();
        assertEquals(expected, outcome);
    }

    @Test
    public void testThatAtLeastConsumedIsCalledWhenASuccess() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        final int initialValue = randomInteger();

        Success.of(initialValue).atLeastConsume(currentValue::set);
        assertEquals(initialValue, currentValue.get());
    }

    @Test
    public void testThatOtherwiseIsNotCalledWhenSuccess() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        final int initialValue = randomInteger();

        Outcome<RuntimeException, Integer> success = Success.of(initialValue);
        Outcome<RuntimeException, Integer> otherwise = success.otherwise(ex -> {
            currentValue.set(initialValue);
            return initialValue;
        });

        assertEquals(0, currentValue.get());
        assertEquals(success, otherwise);
    }

    @Test
    public void testThatOtherwiseIntoIsNotCalledWhenSuccess() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        final int initialValue = randomInteger();

        Outcome<RuntimeException, Integer> success = Success.of(initialValue);
        Outcome<RuntimeException, Integer> otherwise = success.otherwiseTo(ex -> {
            currentValue.set(initialValue);
            return Success.of(initialValue);
        });

        assertEquals(0, currentValue.get());
        assertEquals(success, otherwise);
    }

    @Test
    public void testThatGetOrNullReturnsTheValueWhenSuccess() {
        final int initialValue = randomInteger();
        final int outcome = Success.of(initialValue).getOrNull();

        assertEquals(outcome, initialValue);
    }

    @Test
    public void testThatAFailureIsNotComposedWithAndThen() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        Failure.<RuntimeException, Integer>of(randomException()).andThen(currentValue::getAndSet);
        assertEquals(0, currentValue.get());
    }

    @Test
    public void testThatAFailureIsNotComposedWithAndThenTo() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        Failure.<RuntimeException, Integer>of(randomException())
                .andThenTo(value -> Success.of(currentValue.getAndSet(value)));

        assertEquals(0, currentValue.get());
    }

    @Test
    public void testThatAFailureIsNotComposedWithAtLeastConsume() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        Failure.<RuntimeException, Integer>of(randomException()).atLeastConsume(currentValue::set);

        assertEquals(0, currentValue.get());
    }

    @Test
    public void testThatAFailureIsRecoveredWithOtherwise() {
        final int recoveredValue = randomInteger();
        final int outcome = Failure.<RuntimeException, Integer>of(randomException())
                .otherwise(ex -> recoveredValue)
                .get();

        assertEquals(outcome, recoveredValue);
    }

    @Test
    public void testThatAFailureIsRecoveredWithOtherwiseInto() throws Throwable {
        final int recoveredValue = randomInteger();
        final int outcome = Failure.<RuntimeException, Integer>of(randomException())
                .otherwiseTo(ex -> Success.of(recoveredValue))
                .get();

        assertEquals(outcome, recoveredValue);
    }

    @Test
    public void testThatGetOnAFailureThrowsTheCauseOfFailure() {
        final RuntimeException exception = randomException();
        try {
            Failure.<RuntimeException, Integer>of(exception).get();
            fail("Expected exception was not thrown.");
        } catch (RuntimeException e) {
            assertEquals(exception, e);
        }
    }

    @Test
    public void testThatGetOrNullReturnsNullOnAFailure() {
        assertNull(Failure.<RuntimeException, Integer>of(randomException()).getOrNull());
    }

    @Test
    public void testThatResolveGoesThroughTheFailureBranchWhenFailedOutcome() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        final int failedBranch = randomInteger();
        final int successBranch = randomInteger();

        final int outcome = Failure.of(randomException())
                .resolve(ex -> {
                    currentValue.set(failedBranch);
                    return failedBranch;
                }, v -> currentValue.getAndSet(successBranch));

        assertEquals(outcome, currentValue.get());
        assertEquals(currentValue.get(), failedBranch);
    }

    @Test
    public void testThatASuccessOutcomeIsTransformedToAValidOptional() {
        final int value = randomInteger();
        final Optional<Integer> outcome = Success.of(value).asOptional();

        assertEquals(outcome, Optional.of(value));
    }

    @Test
    public void testThatAFailedOutcomeIsTransformedToAnEmptyOptional() {
        assertEquals(Failure.of(randomException()).asOptional(), Optional.empty());
    }

    @Test
    public void testThatASuccessOutcomeIsTransformedToASuccessCompletes() {
        final Integer value = randomInteger();
        final Completes<Integer> outcome = Success.of(value).asCompletes();

        assertEquals(outcome.outcome(), value);
    }

    @Test
    public void testThatAFailedOutcomeIsTransformedToAFailedCompletes() {
        Completes<Object> completes = Failure.of(randomException()).asCompletes();
        completes.await();

        assertTrue(completes.hasFailed());
    }

    @Test
    public void testThatFilteringInASuccessOutcomeReturnsTheSameOutcome() {
        final Outcome<RuntimeException, Integer> outcome = Success.of(randomInteger());
        final Outcome<NoSuchElementException, Integer> filteredOutcome = outcome.filter(val -> true);

        assertEquals(outcome.get(), filteredOutcome.get());
    }

    @Test(expected = NoSuchElementException.class)
    public void testThatFilteringOutASuccessOutcomeReturnsAFailedOutcome() {
        final Outcome<RuntimeException, Integer> outcome = Success.of(randomInteger());
        final Outcome<NoSuchElementException, Integer> filteredOutcome = outcome.filter(val -> false);

        assertTrue(filteredOutcome instanceof Failure);
        filteredOutcome.get();
    }

    @Test(expected = NoSuchElementException.class)
    public void testThatFilteringInAFailureOutcomeReturnsAFailedOutcome() {
        final Outcome<RuntimeException, Integer> outcome = Failure.of(randomException());
        final Outcome<NoSuchElementException, Integer> filteredOutcome = outcome.filter(val -> true);

        assertTrue(filteredOutcome instanceof Failure);
        filteredOutcome.get();
    }

    @Test
    public void testThatAlongWithReturnsBothSuccessesInATuple() {
        final Outcome<RuntimeException, Integer> first = Success.of(randomInteger());
        final Outcome<RuntimeException, Integer> second = Success.of(randomInteger());

        final Outcome<RuntimeException, Tuple2<Integer, Integer>> wrapped = first.alongWith(second);
        assertEquals(first.get(), wrapped.get()._1);
        assertEquals(second.get(), wrapped.get()._2);
    }

    @Test(expected = RuntimeException.class)
    public void testThatAlongWithReturnsFirstFailure() {
        final Outcome<RuntimeException, Integer> first = Failure.of(randomException());
        final Outcome<RuntimeException, Integer> second = Success.of(randomInteger());

        final Outcome<RuntimeException, Tuple2<Integer, Integer>> wrapped = first.alongWith(second);
        wrapped.get();
    }

    @Test(expected = IOException.class)
    public void testThatOtherwiseFailMapsTheException() throws IOException {
        Failure.of(randomException())
            .otherwiseFail(f -> new IOException(""))
            .get();
    }

    private int randomInteger() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

    private RuntimeException randomException() {
        return new RuntimeException();
    }
}
