package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;
import io.vlingo.common.completes.exceptions.FailedOperationException;

import java.util.function.Consumer;

public class OtherwiseConsume<Receives> extends Operation<Receives, Receives> {
    private final Consumer<Receives> consumer;

    public OtherwiseConsume(Consumer<Receives> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onOutcome(Receives receives) {
        emitOutcome(receives);
    }

    @Override
    public void onError(Exception cause) {
        if (cause instanceof FailedOperationException) {
            Object failureValue = ((FailedOperationException) cause).failureValue;
            consumer.accept((Receives) failureValue);
        }

        emitError(cause);
    }
}
