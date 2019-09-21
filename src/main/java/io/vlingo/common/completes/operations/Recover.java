package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;

import java.util.function.Function;

public class Recover<Receives> extends Operation<Receives, Receives> {
    private final Function<Exception, Receives> recovery;

    public Recover(Function<Exception, Receives> recovery) {
        this.recovery = recovery;
    }

    @Override
    public void onOutcome(Receives receives) {
        emitOutcome(receives);
    }

    @Override
    public void onError(Exception cause) {
        try {
            Receives result = recovery.apply(cause);
            emitOutcome(result);
        } catch (Exception ex) {
            ex.addSuppressed(cause);
            emitError(ex);
        }
    }
}
