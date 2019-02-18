// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Sink<Input, Output> implements Operation<Input, Input, Output> {
    private Input outcome;
    private Throwable error;
    private boolean hasFailed;
    private boolean hasErrored;
    private boolean hasOutcome;
    private boolean completed;
    private CountDownLatch latch = new CountDownLatch(1);

    public final Throwable error() {
        return error;
    }

    public final Input outcome() {
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

    public final <NextOutput> void pipeIfNeeded(Operation<Input, Output, NextOutput> op) {
        if (hasErrored) {
            op.onError(error);
            resetLatch();
        } else if (hasFailed) {
            op.onFailure(outcome);
            resetLatch();
        } else if (completed) {
            op.onOutcome(outcome);
            resetLatch();
        }
    }

    public void repeat() {
        if (isCompleted()) {
            this.outcome = null;
            this.hasOutcome = false;
            this.hasFailed = false;
            this.hasErrored = false;
            this.error = null;
            resetLatch();
        }
    }

    @Override
    public void onOutcome(Input outcome) {
        this.outcome = outcome;
        this.hasOutcome = true;
        this.completed = true;
        latch.countDown();
    }

    @Override
    public void onFailure(Input outcome) {
        this.outcome = outcome;
        this.hasFailed = true;
        this.completed = true;
        latch.countDown();
    }

    @Override
    public void onError(Throwable ex) {
        this.error = ex;
        this.hasErrored = true;
        this.completed = true;
        latch.countDown();
    }

    @Override
    public <LastOutput> void addSubscriber(Operation<Input, Output, LastOutput> operation) {
        throw new IllegalStateException("You can't subscribe to a sink.");
    }

    public Input await(final long timeout) {
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {

        }

        return outcome();
    }

    private void resetLatch() {
        latch = new CountDownLatch(1);
    }
}
