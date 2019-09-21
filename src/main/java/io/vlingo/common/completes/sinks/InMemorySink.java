package io.vlingo.common.completes.sinks;

import io.vlingo.common.*;
import io.vlingo.common.completes.Sink;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class InMemorySink<Exposes> implements Sink<Exposes> {
    private final Scheduler scheduler;
    private Cancellable cancellable;
    private ConcurrentLinkedQueue<Outcome<Exception, Exposes>> outcomes;
    private CountDownLatch latch;
    private AtomicBoolean hasBeenCompleted;

    public InMemorySink(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.outcomes = new ConcurrentLinkedQueue<>();
        this.latch = new CountDownLatch(1);
        this.hasBeenCompleted = new AtomicBoolean(false);
    }

    @Override
    public void onOutcome(Exposes exposes) {
        outcomes.add(Success.of(exposes));
        latch.countDown();
        latch = new CountDownLatch(1);
    }

    @Override
    public void onError(Exception cause) {
        outcomes.add(Failure.of(cause));
        latch.countDown();
        latch = new CountDownLatch(1);
    }

    @Override
    public void onCompletion() {
        hasBeenCompleted.set(true);
    }

    @Override
    public boolean hasBeenCompleted() {
        return hasBeenCompleted.get();
    }

    public boolean hasOutcome() {
        return outcomes.size() > 0 && outcomes.peek().resolve(e -> false, e -> true);
    }

    public boolean hasFailed() {
        return outcomes.size() > 0 && outcomes.peek().resolve(e -> true, e -> false);
    }

    public Optional<Exposes> await() throws Exception {
        try {
            latch.await();
            Outcome<Exception, Exposes> currentOutcome = outcomes.peek();
            if (currentOutcome == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(currentOutcome.get());
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    public Optional<Exposes> await(long timeout) throws Exception {
        try {
            if (latch.await(timeout, TimeUnit.MILLISECONDS)) {
                Outcome<Exception, Exposes> currentOutcome = outcomes.peek();
                if (currentOutcome == null) {
                    return Optional.empty();
                }

                return Optional.ofNullable(currentOutcome.get());
            }
        } catch (InterruptedException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }
}
