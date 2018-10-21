// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

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
    public void testThatASuccessfulOutcomeCanBeComposed() {
        final int initialValue = randomInteger();
        final int successInt = randomInteger();
        final int expected = initialValue * successInt;

        final int outcome = Success.of(initialValue).andThen(value -> value * successInt).get();
        assertEquals(expected, outcome);
    }

    @Test
    public void testThatASuccessfulOutcomeCanBeComposedWithANewOutcome() {
        final int initialValue = randomInteger();
        final int successInt = randomInteger();
        final int expected = initialValue * successInt;

        final int outcome = Success.of(initialValue).andThenInto(value -> Success.of(value * successInt)).get();
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
        Outcome<RuntimeException, Integer> otherwise = success.otherwiseInto(ex -> {
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
    public void testThatAFailureIsNotComposedWithAndThenInto() {
        final AtomicInteger currentValue = new AtomicInteger(0);
        Failure.<RuntimeException, Integer>of(randomException())
                .andThenInto(value -> Success.of(currentValue.getAndSet(value)));

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
    public void testThatAFailureIsRecoveredWithOtherwiseInto() {
        final int recoveredValue = randomInteger();
        final int outcome = Failure.<RuntimeException, Integer>of(randomException())
            .otherwiseInto(ex -> Success.of(recoveredValue))
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

    private int randomInteger() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

    private RuntimeException randomException() {
        return new RuntimeException();
    }
}
