// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.sinks;

import io.vlingo.common.Failure;
import io.vlingo.common.Outcome;
import io.vlingo.common.Scheduler;
import io.vlingo.common.Success;
import io.vlingo.common.completes.Sink;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class InMemorySink<Exposes> implements Sink<Exposes> {
    private ConcurrentLinkedQueue<Outcome<Exception, Exposes>> outcomes;
    private AtomicBoolean hasBeenCompleted;

    public InMemorySink(Scheduler scheduler) {
        this.outcomes = new ConcurrentLinkedQueue<>();
        this.hasBeenCompleted = new AtomicBoolean(false);
    }

    @Override
    public void onOutcome(Exposes exposes) {
        outcomes.add(Success.of(exposes));
    }

    @Override
    public void onError(Exception cause) {
        outcomes.add(Failure.of(cause));
    }

    @Override
    public void onCompletion() {
        hasBeenCompleted.set(true);
    }

    @Override
    public boolean hasBeenCompleted() {
        return hasBeenCompleted.get();
    }

    public boolean hasOutcome() {
        return outcomes.size() > 0 && outcomes.peek().resolve(e -> false, e -> true);
    }

    public boolean hasFailed() {
        return outcomes.size() > 0 && outcomes.peek().resolve(e -> true, e -> false);
    }

    public Optional<Exposes> await() throws Exception {
        try {
            waitUntilOutcomeOrTimeout(Long.MAX_VALUE);
            Outcome<Exception, Exposes> currentOutcome = outcomes.peek();
            if (currentOutcome == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(currentOutcome.get());
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    public Optional<Exposes> await(long timeout) throws Exception {
        try {
            waitUntilOutcomeOrTimeout(timeout);
            Outcome<Exception, Exposes> currentOutcome = outcomes.peek();
            if (currentOutcome == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(currentOutcome.get());
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    private void waitUntilOutcomeOrTimeout(long timeout) throws Exception {
        final long startTime = System.currentTimeMillis();
        do {
            if (!outcomes.isEmpty()) {
                return;
            }
        } while ((System.currentTimeMillis() - startTime) < timeout);
    }
}
