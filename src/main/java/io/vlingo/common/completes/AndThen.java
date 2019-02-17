package io.vlingo.common.completes;

import java.util.function.Function;

public class AndThen<I, O, NO> implements Operation<I, O, NO> {
    private final Function<I, O> mapper;
    private final O failedOutcome;
    private Operation<O, NO, ?> nextOperation;

    public AndThen(Function<I, O> mapper, O failedOutcome) {
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    @Override
    public void onOutcome(I outcome) {
        O next = mapper.apply(outcome);
        if (next == failedOutcome) {
            nextOperation.onFailure(next);
        } else {
            nextOperation.onOutcome(next);
        }
    }

    @Override
    public void onFailure(I outcome) {
        nextOperation.onFailure((O) outcome);
    }

    @Override
    public void onError(Throwable ex) {
        nextOperation.onError(ex);
    }

    @Override
    public <N2O> void addSubscriber(Operation<O, NO, N2O> operation) {
        nextOperation = operation;
    }
}
