package io.vlingo.common.completes;

public interface Source<Exposes> {
    void emitOutcome(Exposes outcome);
    void emitError(Throwable cause);
    void emitCompletion();

    void subscribe(Sink<Exposes> subscriber);
}
