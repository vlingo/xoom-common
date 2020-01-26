// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AndThenConsumeTest extends OperationTest {
    @Test
    public void shouldProcessTheOutcomeButNotChangeIt() {
        AtomicInteger result = new AtomicInteger(0);
        verifier().outcomeIs(42);

        source().emitOutcome(42);
        source().emitCompletion();

        AndThenConsume<Integer> operation = new AndThenConsume<>(result::set);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();

        Assert.assertEquals(42, result.get());
    }

    @Test
    public void shouldPassThroughErrors() {
        Exception cause = new RuntimeException("Yay!");

        verifier().failedWith(cause);

        source().emitError(cause);
        source().emitCompletion();

        AndThenConsume<Integer> operation = new AndThenConsume<>(a -> {
        });
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldEmitAnErrorIfTheMapperFails() {
        verifier().failedWith(NullPointerException.class);

        source().emitOutcome(null);
        source().emitCompletion();

        AndThenConsume<Integer> operation = new AndThenConsume<>(Object::notify);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
