// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.test;

import io.vlingo.common.completes.Sink;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TestSink<Receives> implements Sink<Receives>, SinkVerifier<Receives> {
    private List<Predicate<Receives>> outcomeVerifications;
    private List<Predicate<Exception>> errorCausePredicates;
    private List<Receives> receivedOutcome;
    private List<Exception> receivedErrorCause;
    private boolean hasBeenCompleted;

    public TestSink() {
        outcomeVerifications = new ArrayList<>();
        errorCausePredicates = new ArrayList<>();

        receivedOutcome = new ArrayList<>();
        receivedErrorCause = new ArrayList<>();
        hasBeenCompleted = false;
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
        hasBeenCompleted = true;

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

    @Override
    public boolean hasBeenCompleted() {
        return hasBeenCompleted;
    }

    @Override
    public Optional<Receives> await(long timeout) throws Exception {
        return Optional.empty();
    }

    @Override
    public boolean hasFailed() {
        return false;
    }

    @Override
    public boolean hasOutcome() {
        return false;
    }

    @Override
    public void repeat() {

    }
}
