package io.vlingo.common.completes;

import java.util.function.Function;

public class Otherwise<I, NO> implements Operation<I, I, NO> {
    private final Function<I, I> mapper;
    private Operation<I, NO, ?> nextOperation;

    public Otherwise(Function<I, I> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(I outcome) {
        nextOperation.onOutcome(outcome);
    }

    @Override
    public void onFailure(I outcome) {
        try {
            I next = mapper.apply(outcome);
            nextOperation.onFailure(next);
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