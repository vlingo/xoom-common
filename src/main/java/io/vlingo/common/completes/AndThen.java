// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.barrier.TimeBarrier;

import java.util.function.Function;

public class AndThen<Input, Output, NextOutput> implements Operation<Input, Output, NextOutput> {
    private final TimeBarrier timeBarrier;
    private final Function<Input, Output> mapper;
    private final Output failedOutcome;
    private Operation<Output, NextOutput, ?> nextOperation;

    public AndThen(Scheduler scheduler, long timeout, Function<Input, Output> mapper, Output failedOutcome) {
        this.timeBarrier = new TimeBarrier(scheduler, timeout);
        this.mapper = mapper;
        this.failedOutcome = failedOutcome;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <First> AndThen<First, First, ?> identity(Scheduler scheduler, Sink<First, First> sink) {
        final AndThen<First, First, Object> identity = new AndThen<>(scheduler, 1000, e -> e, null);
        identity.addSubscriber((Operation) sink);
        return identity;
    }

    @Override
    public void onOutcome(Input outcome) {
        timeBarrier.initialize();
        timeBarrier.execute(() -> {
            try {
                Output next = mapper.apply(outcome);
                if (next == failedOutcome) {
                    nextOperation.onFailure(next);
                } else {
                    nextOperation.onOutcome(next);
                }

            } catch (Throwable ex) {
                nextOperation.onError(ex);
            }
        }, nextOperation);
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
    public <N2O> void addSubscriber(Operation<Output, NextOutput, N2O> operation) {
        nextOperation = operation;
    }
}
