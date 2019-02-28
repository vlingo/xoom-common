// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.barrier;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.Operation;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TimeBarrier {
    private static final int INFINITE_TIMEOUT = -1;
    private final Scheduler scheduler;
    private final long timeout;
    private AtomicBoolean didTimeout;
    private Cancellable timeoutCancellable;

    public TimeBarrier(Scheduler scheduler, long timeout) {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.didTimeout = new AtomicBoolean(false);
    }

    public void initialize() {
        if (scheduler != null && timeout != INFINITE_TIMEOUT) {
            timeoutCancellable = scheduler.scheduleOnce(this::raiseTimeout, didTimeout, 0, timeout);
        }
    }

    public void execute(Runnable section, Operation<?, ?, ?> nextOperation) {
        if (scheduler == null || timeout == INFINITE_TIMEOUT) {
            section.run();
        } else {
            if (!didTimeout.get()) {
                section.run();
                timeoutCancellable.cancel();
            } else {
                nextOperation.onError(new TimeoutException());
            }
            didTimeout.set(false);
        }
    }

    @SuppressWarnings("rawtypes")
    private void raiseTimeout(Scheduled scheduled, Object timeout) {
        didTimeout.set(true);
    }
}
