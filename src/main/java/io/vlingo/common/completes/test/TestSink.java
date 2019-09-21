package io.vlingo.common.completes.test;

import io.vlingo.common.completes.Sink;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TestSink<Receives> implements Sink<Receives>, SinkVerifier<Receives> {
    private List<Predicate<Receives>> outcomeVerifications;
    private List<Predicate<Exception>> errorCausePredicates;
    private List<Receives> receivedOutcome;
    private List<Exception> receivedErrorCause;

    public TestSink() {
        outcomeVerifications = new ArrayList<>();
        errorCausePredicates = new ArrayList<>();

        receivedOutcome = new ArrayList<>();
        receivedErrorCause = new ArrayList<>();
    }

    @Override
    public void onOutcome(Receives receives) {
        this.receivedOutcome.add(receives);
    }

    @Override
    public void onError(Exception cause) {
        this.receivedErrorCause.add(cause);
    }

    @Override
    public void onCompletion() {
        if (!outcomeVerifications.isEmpty()) {
            if (outcomeVerifications.size() != receivedOutcome.size()) {
                int numberOfVerifications = outcomeVerifications.size();
                int numberOfOutcomes = receivedOutcome.size();

                throw new AssertionError("Sink was expecting to receive " + numberOfVerifications + " outcomes, but " + numberOfOutcomes + " received.");
            }

            for (int i = 0; i < outcomeVerifications.size(); i++) {
                Receives outcome = receivedOutcome.get(i);
                Predicate<Receives> verification = outcomeVerifications.get(i);

                if (!verification.test(outcome)) {
                    throw new AssertionError("Sink received an outcome but did not fulfill expectations. The received value was:" + outcome);
                }
            }
        }

        if (!errorCausePredicates.isEmpty()) {
            if (errorCausePredicates.size() != receivedErrorCause.size()) {
                int numberOfVerifications = errorCausePredicates.size();
                int numberOfOutcomes = receivedErrorCause.size();

                throw new AssertionError("Sink was expecting to receive " + numberOfVerifications + " errors, but " + numberOfOutcomes + " received.");
            }

            for (int i = 0; i < errorCausePredicates.size(); i++) {
                Exception outcome = receivedErrorCause.get(i);
                Predicate<Exception> verification = errorCausePredicates.get(i);

                if (!verification.test(outcome)) {
                    throw new AssertionError("Sink received an error but did not fulfill expectations. The received value was:" + outcome);
                }
            }
        }
    }

    @Override
    public SinkVerifier<Receives> outcomeIs(Receives receives) {
        outcomeVerifications.add(receives::equals);
        return this;
    }

    @Override
    public SinkVerifier<Receives> outcomeIs(Predicate<Receives> predicate) {
        outcomeVerifications.add(predicate);
        return this;
    }

    @Override
    public SinkVerifier<Receives> failedWith(Exception ex) {
        errorCausePredicates.add(ex::equals);
        return this;
    }

    @Override
    public SinkVerifier<Receives> failedWith(Class<? extends Exception> exClass) {
        errorCausePredicates.add(exClass::isInstance);
        return this;
    }

    @Override
    public SinkVerifier<Receives> failedWith(Predicate<Exception> predicate) {
        errorCausePredicates.add(predicate);
        return this;
    }

    @Override
    public Sink<Receives> asSink() {
        return this;
    }
}
