package io.vlingo.common.completes;

import java.util.function.Function;

public class Recover<I, NO> implements Operation<I, I, NO> {
    private final Function<Throwable, I> mapper;
    private Operation<I, NO, ?> nextOperation;

    public Recover(Function<Throwable, I> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(I outcome) {
        nextOperation.onOutcome(outcome);
    }

    @Override
    public void onFailure(I outcome) {
        nextOperation.onFailure(outcome);
    }

    @Override
    public void onError(Throwable ex) {
        try {
            nextOperation.onOutcome(mapper.apply(ex));
        } catch (Throwable mapperEx) {
            final IllegalStateException mappingEx = new IllegalStateException(mapperEx);
            mappingEx.addSuppressed(ex);
            nextOperation.onError(mappingEx);
        }
    }

    @Override
    public <N2O> void addSubscriber(Operation<I, NO, N2O> operation) {
        nextOperation = operation;
    }
}