package io.vlingo.common.completes.barrier;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.Operation;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TimeBarrier {
    private final Scheduler scheduler;
    private final long timeout;
    private AtomicBoolean didTimeout;
    private Cancellable timeoutCancellable;

    public TimeBarrier(Scheduler scheduler, long timeout) {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.didTimeout = new AtomicBoolean(false);
    }

    public void initialize() {
        if (scheduler != null) {
            timeoutCancellable = scheduler.scheduleOnce(this::raiseTimeout, didTimeout, 0, timeout);
        }
    }

    public void execute(Runnable section, Operation<?, ?, ?> nextOperation) {
        if (scheduler == null) {
            section.run();
        } else {
            if (!didTimeout.get()) {
                section.run();
                timeoutCancellable.cancel();
            } else {
                nextOperation.onError(new TimeoutException());
            }
            didTimeout.set(false);
        }
    }

    private void raiseTimeout(Scheduled scheduled, Object timeout) {
        didTimeout.set(true);
    }
}
