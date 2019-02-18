// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.barrier.TimeBarrier;

import java.util.function.Consumer;

public class AndThenConsume<Input, NextOutput> implements Operation<Input, Input, NextOutput> {
    private final TimeBarrier timeBarrier;
    private final Consumer<Input> consumer;
    private Operation<Input, NextOutput, ?> nextOperation;

    public AndThenConsume(Scheduler scheduler, long timeout, Consumer<Input> consumer) {
        this.timeBarrier = new TimeBarrier(scheduler, timeout);
        this.consumer = consumer;
    }

    @Override
    public void onOutcome(Input outcome) {
        this.timeBarrier.initialize();
        this.timeBarrier.execute(() -> {
            try {
                consumer.accept(outcome);
                nextOperation.onOutcome(outcome);
            } catch (Throwable ex) {
                nextOperation.onError(ex);
            }
        }, nextOperation);
    }

    @Override
    public void onFailure(Input outcome) {
        nextOperation.onFailure(outcome);
    }

    @Override
    public void onError(Throwable ex) {
        nextOperation.onError(ex);
    }

    @Override
    public <LastOutput> void addSubscriber(Operation<Input, NextOutput, LastOutput> operation) {
        this.nextOperation = operation;
    }
}
