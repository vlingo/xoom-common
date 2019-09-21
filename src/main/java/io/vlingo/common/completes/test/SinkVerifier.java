package io.vlingo.common.completes.test;

import io.vlingo.common.completes.Sink;

import java.util.function.Predicate;

public interface SinkVerifier<Outcome> {
    SinkVerifier<Outcome> outcomeIs(Outcome outcome);
    SinkVerifier<Outcome> outcomeIs(Predicate<Outcome> predicate);

    SinkVerifier<Outcome> failedWith(Exception ex);
    SinkVerifier<Outcome> failedWith(Class<? extends Exception> exClass);
    SinkVerifier<Outcome> failedWith(Predicate<Exception> predicate);

    Sink<Outcome> asSink();
}
