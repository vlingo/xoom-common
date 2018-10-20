package io.vlingo.common.fn.outcome;

import org.junit.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class OutcomeConversionsTest {
    @Test
    public void testThatASuccessOutcomeIsTransformedToAValidOptional() {
        final int value = randomInteger();
        final Optional<Integer> outcome = OutcomeConversions.asOptional(Success.of(value));

        assertEquals(outcome, Optional.of(value));
    }

    @Test
    public void testThatAFailedOutcomeIsTransformedToAnEmptyOptional() {
        final Optional<Integer> outcome = OutcomeConversions.asOptional(Failure.of(randomException()));

        assertEquals(outcome, Optional.empty());
    }

    private int randomInteger() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

    private RuntimeException randomException() {
        return new RuntimeException();
    }
}
