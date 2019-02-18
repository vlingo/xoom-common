package io.vlingo.common.completes;

import java.util.function.Function;

public class Otherwise<Input, NextOutput> implements Operation<Input, Input, NextOutput> {
    private final Function<Input, Input> mapper;
    private Operation<Input, NextOutput, ?> nextOperation;

    public Otherwise(Function<Input, Input> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(Input outcome) {
        nextOperation.onOutcome(outcome);
    }

    @Override
    public void onFailure(Input outcome) {
        try {
            Input next = mapper.apply(outcome);
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
    public <LastOutput> void addSubscriber(Operation<Input, NextOutput, LastOutput> operation) {
        nextOperation = operation;
    }
}