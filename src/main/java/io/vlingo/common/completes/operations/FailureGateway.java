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
