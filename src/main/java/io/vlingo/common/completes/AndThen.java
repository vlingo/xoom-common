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

    public static <T> AndThen<T, T, ?> identity(Sink<T, T> sink) {
        final AndThen<T, T, Object> identity = new AndThen<>(e -> e, null);
        identity.addSubscriber((Operation) sink);
        return identity;
    }

    @Override
    public void onOutcome(I outcome) {
        try {
            O next = mapper.apply(outcome);
            if (next == failedOutcome) {
                nextOperation.onFailure(next);
            } else {
                nextOperation.onOutcome(next);
            }
        } catch (Throwable ex) {
            nextOperation.onError(ex);
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
