package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;

import java.util.function.Consumer;

public class AndThenConsume<Receives> extends Operation<Receives, Receives> {
    private final Consumer<Receives> consumer;

    public AndThenConsume(Consumer<Receives> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onOutcome(Receives receives) {
        try {
            consumer.accept(receives);
            emitOutcome(receives);
        } catch (Throwable ex) {
            emitError(ex);
        }
    }
}
