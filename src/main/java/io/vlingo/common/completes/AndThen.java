package io.vlingo.common.completes;

import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.barrier.TimeBarrier;

import java.util.function.Function;

public class AndThen<I, O, NO> implements Operation<I, O, NO> {
    private final TimeBarrier timeBarrier;
    private final Function<I, O> mapper;
    private final O failedOutcome;
    private Operation<O, NO, ?> nextOperation;

    public AndThen(Scheduler scheduler, long timeout, Function<I, O> mapper, O failedOutcome) {
        this.timeBarrier = new TimeBarrier(scheduler, timeout);
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    public static <T> AndThen<T, T, ?> identity(Scheduler scheduler, Sink<T, T> sink) {
        final AndThen<T, T, Object> identity = new AndThen<>(scheduler, 1000, e -> e, null);
        identity.addSubscriber((Operation) sink);
        return identity;
    }

    @Override
    public void onOutcome(I outcome) {
        timeBarrier.initialize();
        timeBarrier.execute(() -> {
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
        }, nextOperation);
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
