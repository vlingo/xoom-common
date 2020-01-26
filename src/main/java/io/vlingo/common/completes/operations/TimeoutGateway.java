// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.Operation;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimeoutGateway<Receives> extends Operation<Receives, Receives> implements Scheduled<Void> {
    private final Scheduler scheduler;
    private final long timeout;
    private Cancellable cancellable;
    private AtomicBoolean didTimeout;

    public TimeoutGateway(Scheduler scheduler, long timeout) {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.didTimeout = new AtomicBoolean(false);
        startTimer();
    }

    @Override
    public void onOutcome(Receives receives) {
        if (!didTimeout.get()) {
            this.cancellable.cancel();
            emitOutcome(receives);
            startTimer();
        }
    }

    @Override
    public void intervalSignal(Scheduled<Void> scheduled, Void data) {
        emitError(new TimeoutException());
        didTimeout.set(true);
    }

    private void startTimer() {
        this.cancellable = scheduler.scheduleOnce(this, null, 0, timeout);
        this.didTimeout.set(false);
    }
}
