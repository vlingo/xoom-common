package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;

import java.util.function.Function;

public class Otherwise<Receives> extends Operation<Receives, Receives> {
    private final Function<Throwable, Receives> recovery;

    public Otherwise(Function<Throwable, Receives> recovery) {
        this.recovery = recovery;
    }

    @Override
    public void onOutcome(Receives receives) {
        emitOutcome(receives);
    }

    @Override
    public void onError(Throwable cause) {
        try {
            Receives result = recovery.apply(cause);
            emitOutcome(result);
        } catch (Throwable ex) {
            ex.addSuppressed(cause);
            emitError(ex);
        }
    }
}
