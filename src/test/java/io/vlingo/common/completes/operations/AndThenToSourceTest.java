// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.test.TestSource;
import org.junit.Test;

public class AndThenToSourceTest extends OperationTest {
    @Test
    public void shouldProcessTheOutcomeButNotChangeIt() {
        verifier().outcomeIs(2);
        verifier().outcomeIs(4);

        source().emitOutcome(2);
        source().emitCompletion();

        AndThenToSource<Integer, Integer> operation = new AndThenToSource<>(v -> {
            TestSource<Integer> multiplier = new TestSource<>();
            multiplier.emitOutcome(v);
            multiplier.emitOutcome(v * 2);
            multiplier.emitCompletion();
            multiplier.flush();

            return multiplier;
        });

        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldEmitAnyErrorReceivedOnTheSourceStream() {
        verifier().outcomeIs(2);
        verifier().outcomeIs(4);
        verifier().failedWith(NullPointerException.class);

        source().emitOutcome(2);
        source().emitOutcome(null);
        source().emitCompletion();

        AndThenToSource<Integer, Integer> operation = new AndThenToSource<>(v -> {
            TestSource<Integer> multiplier = new TestSource<>();
            multiplier.emitOutcome(v);
            multiplier.emitOutcome(v * 2);
            multiplier.emitCompletion();
            multiplier.flush();

            return multiplier;
        });

        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
