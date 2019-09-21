// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import org.junit.Test;

public class AndThenTest extends OperationTest {
    @Test
    public void shouldMapAValueAndExposeTheOutcome() {
        verifier().outcomeIs(420);

        source().emitOutcome(42);
        source().emitCompletion();

        AndThen<Integer, Integer> operation = new AndThen<>(a -> a * 10);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldPassThroughErrors() {
        Exception cause = new RuntimeException("Yay!");

        verifier().failedWith(cause);

        source().emitError(cause);
        source().emitCompletion();

        AndThen<Integer, Integer> operation = new AndThen<>(a -> a * 10);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldEmitAnErrorIfTheMapperFails() {
        verifier().failedWith(NullPointerException.class);

        source().emitOutcome(null);
        source().emitCompletion();

        AndThen<Integer, Integer> operation = new AndThen<>(a -> a * 10);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
