package io.vlingo.common.completes;

public abstract class Operation<Receives, Exposes> implements Sink<Receives>, Source<Exposes> {
    private Sink<Exposes> subscriber;

    @Override
    public void onError(Exception cause) {
        emitError(cause);
    }

    @Override
    public void onCompletion() {
        emitCompletion();
    }

    @Override
    public void emitOutcome(Exposes outcome) {
        subscriber.onOutcome(outcome);
    }

    @Override
    public void emitError(Exception cause) {
        subscriber.onError(cause);
    }

    @Override
    public void emitCompletion() {
        subscriber.onCompletion();
    }

    @Override
    public boolean hasBeenCompleted() {
        return subscriber.hasBeenCompleted();
    }

    @Override
    public void subscribe(Sink<Exposes> subscriber) {
        this.subscriber = subscriber;
    }
}
