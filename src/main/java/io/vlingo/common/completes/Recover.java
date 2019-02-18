package io.vlingo.common.completes;

import java.util.function.Function;

public class Recover<Input, NextOutput> implements Operation<Input, Input, NextOutput> {
    private final Function<Throwable, Input> mapper;
    private Operation<Input, NextOutput, ?> nextOperation;

    public Recover(Function<Throwable, Input> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(Input outcome) {
        nextOperation.onOutcome(outcome);
    }

    @Override
    public void onFailure(Input outcome) {
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
    public <LastOutput> void addSubscriber(Operation<Input, NextOutput, LastOutput> operation) {
        nextOperation = operation;
    }
}