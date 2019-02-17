package io.vlingo.common.completes;

import java.util.function.Consumer;

public class AndThenConsume<I, NO> implements Operation<I, I, NO> {
    private final Consumer<I> consumer;
    private Operation<I, NO, ?> nextOperation;

    public AndThenConsume(Consumer<I> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onOutcome(I outcome) {
        consumer.accept(outcome);
        nextOperation.onOutcome(outcome);
    }

    @Override
    public void onFailure(I outcome) {
        nextOperation.onFailure(outcome);
    }

    @Override
    public void onError(Throwable ex) {
        nextOperation.onError(ex);
    }

    @Override
    public <N2O> void addSubscriber(Operation<I, NO, N2O> operation) {
        this.nextOperation = operation;
    }

}
