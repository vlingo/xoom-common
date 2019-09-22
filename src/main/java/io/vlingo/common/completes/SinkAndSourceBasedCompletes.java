// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.operations.*;
import io.vlingo.common.completes.sinks.InMemorySink;
import io.vlingo.common.completes.sources.InMemorySource;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SinkAndSourceBasedCompletes<T> implements Completes<T> {
    private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;

    private final Scheduler scheduler;
    public final InMemorySource<Object> source;
    private final Source<T> currentOperation;
    private final InMemorySink<T> sink;

    private SinkAndSourceBasedCompletes(Scheduler scheduler, InMemorySource<Object> source, Source<T> currentOperation, InMemorySink<T> sink) {
        this.scheduler = scheduler;
        this.source = source;
        this.sink = sink;
        this.currentOperation = currentOperation;
    }

    public static <T> SinkAndSourceBasedCompletes<T> withScheduler(Scheduler scheduler) {
        InMemorySource<T> source = new InMemorySource<>();
        InMemorySink<T> sink = new InMemorySink<>();

        source.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, (InMemorySource<Object>) source, source, sink);
    }

    public static boolean isToggleActive() {
        return Boolean.parseBoolean(System.getProperty("vlingo.InMemoryCompletes", "false"));
    }

    @Override
    public <O> SinkAndSourceBasedCompletes<O> andThen(long timeout, O failedOutcomeValue, Function<T, O> function) {
        FailureGateway<O> failureGateway = new FailureGateway<>(failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, O> newSource = new AndThen<>(function);
        currentOperation.subscribe(timeoutGateway);
        timeoutGateway.subscribe(newSource);
        newSource.subscribe(failureGateway);
        failureGateway.subscribe((InMemorySink<O>) sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, failureGateway, (InMemorySink<O>) sink);
    }

    @Override
    public <O> SinkAndSourceBasedCompletes<O> andThen(O failedOutcomeValue, Function<T, O> function) {
        return andThen(DEFAULT_TIMEOUT, failedOutcomeValue, function);
    }

    @Override
    public <O> SinkAndSourceBasedCompletes<O> andThen(long timeout, Function<T, O> function) {
        return andThen(timeout, null, function);
    }

    @Override
    public <O> SinkAndSourceBasedCompletes<O> andThen(Function<T, O> function) {
        return andThen(DEFAULT_TIMEOUT, null, function);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> andThenConsume(long timeout, T failedOutcomeValue, Consumer<T> consumer) {
        FailureGateway<T> failureGateway = new FailureGateway<>(failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, T> newSource = new AndThenConsume<>(consumer);
        currentOperation.subscribe(timeoutGateway);
        timeoutGateway.subscribe(newSource);
        newSource.subscribe(failureGateway);
        failureGateway.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, failureGateway, sink);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> andThenConsume(T failedOutcomeValue, Consumer<T> consumer) {
        return andThenConsume(DEFAULT_TIMEOUT, failedOutcomeValue, consumer);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> andThenConsume(long timeout, Consumer<T> consumer) {
        return andThenConsume(timeout, null, consumer);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> andThenConsume(Consumer<T> consumer) {
        return andThenConsume(DEFAULT_TIMEOUT, null, consumer);
    }

    @Override
    public <F, O> O andThenTo(long timeout, F failedOutcomeValue, Function<T, O> function) {
        FailureGateway<O> failureGateway = new FailureGateway<>((O) failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, O> newSource = new AndThenToSource<>(function.andThen(e -> (SinkAndSourceBasedCompletes<O>) e).andThen(InMemorySource::fromCompletes));

        currentOperation.subscribe(timeoutGateway);
        timeoutGateway.subscribe(newSource);
        newSource.subscribe(failureGateway);
        failureGateway.subscribe((InMemorySink<O>) sink);

        return (O) new SinkAndSourceBasedCompletes<>(scheduler, source, failureGateway, (InMemorySink<O>) sink);
    }

    @Override
    public <F, O> O andThenTo(F failedOutcomeValue, Function<T, O> function) {
        return andThenTo(DEFAULT_TIMEOUT, failedOutcomeValue, function);
    }

    @Override
    public <O> O andThenTo(long timeout, Function<T, O> function) {
        return andThenTo(timeout, null, function);
    }

    @Override
    public <O> O andThenTo(Function<T, O> function) {
        return andThenTo(DEFAULT_TIMEOUT, null, function);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> otherwise(Function<T, T> function) {
        Operation<T, T> otherwise = new Otherwise<>(function);
        currentOperation.subscribe(otherwise);
        otherwise.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, otherwise, sink);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> otherwiseConsume(Consumer<T> consumer) {
        Operation<T, T> otherwise = new OtherwiseConsume<>(consumer);
        currentOperation.subscribe(otherwise);
        otherwise.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, otherwise, sink);
    }

    @Override
    public SinkAndSourceBasedCompletes<T> recoverFrom(Function<Exception, T> function) {
        Operation<T, T> newSource = new Recover<>(function);
        currentOperation.subscribe(newSource);
        newSource.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, newSource, sink);
    }

    @Override
    public <O> O await() {
        return await(DEFAULT_TIMEOUT);
    }

    @Override
    public <O> O await(long timeout) {
        source.activate();

        try {
            Optional<T> value = sink.await(timeout);
            if (value.isPresent()) {
                return (O) value.get();
            }

        } catch (Exception e) {
            return null;
        }

        return null;
    }

    @Override
    public boolean isCompleted() {
        return sink.hasBeenCompleted();
    }

    @Override
    public boolean hasFailed() {
        return sink.hasFailed();
    }

    @Override
    public void failed() {
        source.emitError(new IllegalStateException("Forced failure in Completes"));
    }

    @Override
    public boolean hasOutcome() {
        return sink.hasOutcome();
    }

    @Override
    public T outcome() {
        return await();
    }

    @Override
    public Completes<T> repeat() {
        sink.repeat();
        return this;
    }

    @Override
    public void andFinallyConsume(Consumer<T> consumer) {
        SinkAndSourceBasedCompletes<T> edge = andThenConsume(consumer);
        edge.source.activate();
    }

    @Override
    public <O> SinkAndSourceBasedCompletes<O> with(O outcome) {
        source.emitOutcome(outcome);
        return (SinkAndSourceBasedCompletes<O>) this;
    }
}
