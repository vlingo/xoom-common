// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;
import io.vlingo.common.completes.exceptions.FailedOperationException;

import java.util.function.Function;

public class Otherwise<Receives> extends Operation<Receives, Receives> {
    private final Function<Receives, Receives> recoveryFunction;

    public Otherwise(Function<Receives, Receives> recoveryFunction) {
        this.recoveryFunction = recoveryFunction;
    }

    @Override
    public void onOutcome(Receives receives) {
        emitOutcome(receives);
    }

    @Override
    public void onError(Exception cause) {
        if (cause instanceof FailedOperationException) {
            Object failureValue = ((FailedOperationException) cause).failureValue;
            emitOutcome(recoveryFunction.apply((Receives) failureValue));
        } else {
            emitError(cause);
        }
    }
}
