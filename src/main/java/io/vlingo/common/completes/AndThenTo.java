package io.vlingo.common.completes;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.barrier.TimeBarrier;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class AndThenTo<I, O, NO> implements Operation<I, O, NO> {
    private final TimeBarrier timeBarrier;
    private final Function<I, Completes<O>> mapper;
    private final O failedOutcome;
    private Operation<O, NO, ?> nextOperation;
    private Cancellable timeoutCancellable;

    public AndThenTo(Scheduler scheduler, long timeout, Function<I, Completes<O>> mapper, O failedOutcome) {
        this.timeBarrier = new TimeBarrier(scheduler, timeout);
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    @Override
    public void onOutcome(I outcome) {
        Completes<O> completes = mapper.apply(outcome);
        this.timeBarrier.initialize();
        completes.andThenConsume(v -> {
            this.timeBarrier.execute(() -> {
                if (v == failedOutcome) {
                    nextOperation.onFailure(v);
                } else {
                    nextOperation.onOutcome(v);
                }
            }, nextOperation);
        }).otherwiseConsume(v -> {
            timeBarrier.execute(() -> {
                nextOperation.onFailure(v);
                timeoutCancellable.cancel();
            }, nextOperation);
        });
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