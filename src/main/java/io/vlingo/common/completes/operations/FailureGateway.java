// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;
import io.vlingo.common.completes.exceptions.FailedOperationException;

public class FailureGateway<Receives> extends Operation<Receives, Receives> {
    private final Receives failureOutcome;

    public FailureGateway(Receives failureOutcome) {
        this.failureOutcome = failureOutcome;
    }

    @Override
    public void onOutcome(Receives receives) {
        if (receives == failureOutcome) {
            emitError(new FailedOperationException(receives));
        } else {
            emitOutcome(receives);
        }
    }
}
