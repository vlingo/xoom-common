package io.vlingo.common.completes;

import java.util.function.Function;

public class OtherwiseConsume<I, NO> implements Operation<I, I, NO> {
    private final Function<I, Void> mapper;
    private Operation<I, NO, ?> nextOperation;

    public OtherwiseConsume(Function<I, Void> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(I outcome) {
        mapper.apply(outcome);
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
        nextOperation = operation;
    }
}