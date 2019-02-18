package io.vlingo.common.completes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Sink<I, O> implements Operation<I, I, O> {
    private I outcome;
    private Throwable error;
    private boolean hasFailed;
    private boolean hasErrored;
    private boolean hasOutcome;
    private boolean completed;
    private CountDownLatch latch = new CountDownLatch(1);

    public final Throwable error() {
        return error;
    }

    public final I outcome() {
        return outcome;
    }

    public final boolean hasOutcome() {
        return hasOutcome;
    }

    public final boolean hasFailed() {
        return hasFailed;
    }

    public final boolean hasErrored() {
        return hasErrored;
    }

    public final boolean isCompleted() {
        return hasOutcome() || hasFailed() || hasErrored();
    }

    public final <NO> void pipeIfNeeded(Operation<I, O, NO> op) {
        if (hasErrored) {
            op.onError(error);
            resetLatch();
        } else if (hasFailed) {
            op.onFailure(outcome);
            resetLatch();
        } else if (completed) {
            op.onOutcome(outcome);
            resetLatch();
        }
    }

    public void repeat() {
        if (isCompleted()) {
            this.outcome = null;
            this.hasOutcome = false;
            this.hasFailed = false;
            this.hasErrored = false;
            this.error = null;
            resetLatch();
        }
    }

    @Override
    public void onOutcome(I outcome) {
        this.outcome = outcome;
        this.hasOutcome = true;
        this.completed = true;
        latch.countDown();
    }

    @Override
    public void onFailure(I outcome) {
        this.outcome = outcome;
        this.hasFailed = true;
        this.completed = true;
        latch.countDown();
    }

    @Override
    public void onError(Throwable ex) {
        this.error = ex;
        this.hasErrored = true;
        this.completed = true;
        latch.countDown();
    }

    @Override
    public <N2O> void addSubscriber(Operation<I, O, N2O> operation) {
        throw new IllegalStateException("You can't subscribe to a sink.");
    }

    public I await(final long timeout) {
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {

        }

        return outcome();
    }

    private void resetLatch() {
        latch = new CountDownLatch(1);
    }
}
