package io.vlingo.common.completes;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class AndThenTo<I, O, NO> implements Operation<I, O, NO> {
    private final Scheduler scheduler;
    private final int timeout;
    private final Function<I, Completes<O>> mapper;
    private final O failedOutcome;
    private Operation<O, NO, ?> nextOperation;
    private Cancellable timeoutCancellable;
    private Cancellable outcomeChecker;

    public AndThenTo(Scheduler scheduler, int timeout, Function<I, Completes<O>> mapper, O failedOutcome) {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    @Override
    public void onOutcome(I outcome) {
        Completes<O> completes = mapper.apply(outcome);
        timeoutCancellable = scheduler.scheduleOnce(this::raiseTimeout, null, 0, timeout);
        outcomeChecker = scheduler.schedule(this::propagateOutcome, completes, 0, 0);
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

    private void raiseTimeout(Scheduled scheduled, Object nullData) {
        this.onError(new TimeoutException());
        this.outcomeChecker.cancel();
        this.timeoutCancellable.cancel();
    }

    private void propagateOutcome(Scheduled scheduled, Object completesAny) {
        Completes<O> completes = (Completes<O>) completesAny;
        completes.andThenConsume(next -> {
            timeoutCancellable.cancel();
            outcomeChecker.cancel();

            if (next == failedOutcome) {
                nextOperation.onFailure(next);
            } else {
                nextOperation.onOutcome(next);
            }
        }).otherwiseConsume(nextOperation::onOutcome);
    }
}