// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import org.junit.Test;

import io.vlingo.common.Failure;
import io.vlingo.common.OutcomeConversions;
import io.vlingo.common.Success;

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
