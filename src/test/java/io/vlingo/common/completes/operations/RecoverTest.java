// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import org.junit.Test;

public class RecoverTest extends OperationTest {
    @Test
    public void shouldRecoverFromAnError() {
        verifier().outcomeIs(4);

        source().emitError(new NullPointerException());
        source().emitCompletion();

        Recover<Integer> operation = new Recover<>(cause -> 4);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldFailIfTheErrorRecoveryThrowsAnExceptionAndAddSourceExceptionAsSuppressed() {
        verifier().failedWith(p -> p.getSuppressed()[0].getClass() == IllegalArgumentException.class);

        source().emitError(new IllegalArgumentException());
        source().emitCompletion();

        Recover<Integer> operation = new Recover<>(cause -> { throw new NullPointerException(); });
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
