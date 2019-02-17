package io.vlingo.common.completes;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class AndThenTo<I, O, NO> implements Operation<I, O, NO> {
    private final Scheduler scheduler;
    private final long timeout;
    private final Function<I, Completes<O>> mapper;
    private final O failedOutcome;
    private Operation<O, NO, ?> nextOperation;
    private Cancellable timeoutCancellable;

    public AndThenTo(Scheduler scheduler, long timeout, Function<I, Completes<O>> mapper, O failedOutcome) {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    @Override
    public void onOutcome(I outcome) {
        Completes<O> completes = mapper.apply(outcome);
        AtomicBoolean didTimeout = new AtomicBoolean(false);
        completes.andThenConsume(v -> {
            if (!didTimeout.get()) {
                if (v == failedOutcome) {
                    nextOperation.onFailure(v);
                } else {
                    nextOperation.onOutcome(v);
                }
                timeoutCancellable.cancel();
            }
        }).otherwiseConsume(v -> {
            if (!didTimeout.get()) {
                nextOperation.onFailure(v);
                timeoutCancellable.cancel();
            }
        });

        timeoutCancellable = scheduler.scheduleOnce(this::raiseTimeout, didTimeout, 0, timeout);
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

    private void raiseTimeout(Scheduled scheduled, Object timeout) {
        AtomicBoolean didTimeout = (AtomicBoolean) timeout;
        this.onError(new TimeoutException());
        didTimeout.set(true);
    }
}