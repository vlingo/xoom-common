package io.vlingo.common.completes;

public class Sink<I, O> implements Operation<I, I, O> {
    protected I outcome;
    protected Throwable error;
    protected boolean hasFailed;
    protected boolean hasErrored;
    protected boolean hasOutcome;
    protected boolean completed;

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
        } else if (hasFailed) {
            op.onFailure(outcome);
        } else if (completed) {
            op.onOutcome(outcome);
        }
    }

    public void repeat() {
        if (isCompleted()) {
            this.outcome = null;
            this.hasOutcome = false;
            this.hasFailed = false;
            this.hasErrored = false;
            this.error = null;
        }
    }

    @Override
    public void onOutcome(I outcome) {
        this.outcome = outcome;
        this.hasOutcome = true;
        this.completed = true;
    }

    @Override
    public void onFailure(I outcome) {
        this.outcome = outcome;
        this.hasFailed = true;
        this.completed = true;
    }

    @Override
    public void onError(Throwable ex) {
        this.error = ex;
        this.hasErrored = true;
        this.completed = true;
    }

    @Override
    public <N2O> void addSubscriber(Operation<I, O, N2O> operation) {
        throw new IllegalStateException("You can't subscribe to a sink.");
    }
}
