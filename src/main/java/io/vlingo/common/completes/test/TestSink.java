package io.vlingo.common.completes.test;

import io.vlingo.common.completes.Sink;

import java.util.Optional;
import java.util.function.Predicate;

public class TestSink<Receives> implements Sink<Receives>, SinkVerifier<Receives> {
    private Predicate<Receives> outcomeVerifications;
    private boolean hasOutcomeVerifications;
    private Predicate<Throwable> errorCausePredicates;
    private boolean hasCausePredicates;

    private Optional<Receives> receivedOutcome;
    private Optional<Throwable> receivedErrorCause;

    public TestSink() {
        outcomeVerifications = (r) -> true;
        errorCausePredicates = (r) -> true;
        hasOutcomeVerifications = false;
        hasCausePredicates = false;

        receivedOutcome = Optional.empty();
        receivedErrorCause = Optional.empty();
    }

    @Override
    public void onOutcome(Receives receives) {
        this.receivedOutcome = Optional.of(receives);
    }

    @Override
    public void onError(Throwable cause) {
        this.receivedErrorCause = Optional.of(cause);
    }

    @Override
    public void onCompletion() {
        if (hasOutcomeVerifications) {
            if (!receivedOutcome.isPresent()) {
                throw new AssertionError("Sink was expecting to receive an outcome, but nothing arrived.");
            }

            Receives outcome = receivedOutcome.get();
            if (!outcomeVerifications.test(outcome)) {
                throw new AssertionError("Sink received an outcome but did not fulfill expectations. The received value was:" + outcome);
            }
        }

        if (hasCausePredicates) {
            if (!receivedErrorCause.isPresent()) {
                throw new AssertionError("Sink was expecting to receive a failure, but completed successfully.");
            }

            Throwable cause = receivedErrorCause.get();
            if (!errorCausePredicates.test(cause)) {
                throw new AssertionError("Sink received a failure, but did not fulfill expectations. The received error was:" + cause, cause);
            }
        }
    }

    @Override
    public SinkVerifier<Receives> outcomeIs(Receives receives) {
        outcomeVerifications = outcomeVerifications.and(receives::equals);
        hasOutcomeVerifications = true;
        return this;
    }

    @Override
    public SinkVerifier<Receives> outcomeIs(Predicate<Receives> predicate) {
        outcomeVerifications = outcomeVerifications.and(predicate);
        hasOutcomeVerifications = true;
        return this;
    }

    @Override
    public SinkVerifier<Receives> failedWith(Throwable ex) {
        errorCausePredicates = errorCausePredicates.and(ex::equals);
        hasCausePredicates = true;
        return this;
    }

    @Override
    public SinkVerifier<Receives> failedWith(Class<? extends Throwable> exClass) {
        errorCausePredicates = errorCausePredicates.and(exClass::isInstance);
        hasCausePredicates = true;
        return this;
    }

    @Override
    public SinkVerifier<Receives> failedWith(Predicate<Throwable> predicate) {
        errorCausePredicates = errorCausePredicates.and(predicate);
        hasCausePredicates = true;
        return this;
    }

    @Override
    public Sink<Receives> asSink() {
        return this;
    }
}
