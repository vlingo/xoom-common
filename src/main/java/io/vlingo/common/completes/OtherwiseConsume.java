package io.vlingo.common.completes;

import java.util.function.Consumer;

public class OtherwiseConsume<I, NO> implements Operation<I, I, NO> {
    private final Consumer<I> mapper;
    private Operation<I, NO, ?> nextOperation;

    public OtherwiseConsume(Consumer<I> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(I outcome) {
        nextOperation.onOutcome(outcome);
    }

    @Override
    public void onFailure(I outcome) {
        try {
            mapper.accept(outcome);
            nextOperation.onFailure(outcome);
        } catch (Throwable ex) {
            nextOperation.onError(ex);
        }
    }

    @Override
    public void onError(Throwable ex) {
        nextOperation.onError(ex);
    }

    @Override
    public <N2O> void addSubscriber(Operation<I, NO, N2O> operation) {
        nextOperation = operation;
    }
}