// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import java.util.Optional;

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

    @Override
    public void activate() {

    }

    @Override
    public Optional<Receives> await(long timeout) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
