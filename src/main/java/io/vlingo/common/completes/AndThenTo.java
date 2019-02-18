// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.barrier.TimeBarrier;

import java.util.function.Function;

public class AndThenTo<Input, Output, NextOutput> implements Operation<Input, Output, NextOutput> {
    private final TimeBarrier timeBarrier;
    private final Function<Input, Completes<Output>> mapper;
    private final Output failedOutcome;
    private Operation<Output, NextOutput, ?> nextOperation;

    public AndThenTo(Scheduler scheduler, long timeout, Function<Input, Completes<Output>> mapper, Output failedOutcome) {
        this.timeBarrier = new TimeBarrier(scheduler, timeout);
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    @Override
    public void onOutcome(Input outcome) {
        Completes<Output> completes = mapper.apply(outcome);
        this.timeBarrier.initialize();
        completes.andThenConsume(v -> {
            this.timeBarrier.execute(() -> {
                if (v == failedOutcome) {
                    nextOperation.onFailure(v);
                } else {
                    nextOperation.onOutcome(v);
                }
            }, nextOperation);
        }).otherwiseConsume(v -> {
            timeBarrier.execute(() -> {
                nextOperation.onFailure(v);
            }, nextOperation);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onFailure(Input outcome) {
        nextOperation.onFailure((Output) outcome);
    }

    @Override
    public void onError(Throwable ex) {
        nextOperation.onError(ex);
    }

    @Override
    public <LastOutput> void addSubscriber(Operation<Output, NextOutput, LastOutput> operation) {
        nextOperation = operation;
    }
}