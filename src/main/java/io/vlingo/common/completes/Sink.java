package io.vlingo.common.completes;

public interface Sink<Receives> {
    void onOutcome(Receives receives);
    void onError(Exception cause);
    void onCompletion();
    boolean hasBeenCompleted();
}
