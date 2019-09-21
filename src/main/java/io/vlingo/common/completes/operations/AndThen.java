package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;

import java.util.function.Function;

public class AndThen<Receives, Exposes> extends Operation<Receives, Exposes> {
    private final Function<Receives, Exposes> mapper;

    public AndThen(Function<Receives, Exposes> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(Receives receives) {
        try {
            Exposes outcome = mapper.apply(receives);
            emitOutcome(outcome);
        } catch (Throwable ex) {
            emitError(ex);
        }
    }
}
