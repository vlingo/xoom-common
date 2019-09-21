package io.vlingo.common.completes;

public interface Sink<Receives> {
    void onOutcome(Receives receives);
    void onError(Throwable cause);
    void onCompletion();
}
