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

public class InMemoryCompletes<T> implements Completes<T> {
    private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;

    private final Scheduler scheduler;
    private final InMemorySource<Object> source;
    private final Source<T> currentOperation;
    private final InMemorySink<T> sink;

    private InMemoryCompletes(Scheduler scheduler, InMemorySource<Object> source, Source<T> currentOperation, InMemorySink<T> sink) {
        this.scheduler = scheduler;
        this.source = source;
        this.sink = sink;
        this.currentOperation = currentOperation;
    }

    public static <T> InMemoryCompletes<T> withScheduler(Scheduler scheduler) {
        InMemorySource<T> source = new InMemorySource<>();
        InMemorySink<T> sink = new InMemorySink<>();

        source.subscribe(sink);

        return new InMemoryCompletes<>(scheduler, (InMemorySource<Object>) source, source, sink);
    }

    public static boolean isToggleActive() {
        return Boolean.parseBoolean(System.getProperty("vlingo.InMemoryCompletes", "false"));
    }

    @Override
    public <O> Completes<O> andThen(long timeout, O failedOutcomeValue, Function<T, O> function) {
        FailureGateway<O> failureGateway = new FailureGateway<>(failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, O> newSource = new AndThen<>(function);
        currentOperation.subscribe(timeoutGateway);
        timeoutGateway.subscribe(newSource);
        newSource.subscribe(failureGateway);
        failureGateway.subscribe((InMemorySink<O>) sink);

        return new InMemoryCompletes<>(scheduler, source, failureGateway, (InMemorySink<O>) sink);
    }

    @Override
    public <O> Completes<O> andThen(O failedOutcomeValue, Function<T, O> function) {
        return andThen(DEFAULT_TIMEOUT, failedOutcomeValue, function);
    }

    @Override
    public <O> Completes<O> andThen(long timeout, Function<T, O> function) {
        return andThen(timeout, null, function);
    }

    @Override
    public <O> Completes<O> andThen(Function<T, O> function) {
        return andThen(DEFAULT_TIMEOUT, null, function);
    }

    @Override
    public Completes<T> andThenConsume(long timeout, T failedOutcomeValue, Consumer<T> consumer) {
        FailureGateway<T> failureGateway = new FailureGateway<>(failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, T> newSource = new AndThenConsume<>(consumer);
        currentOperation.subscribe(timeoutGateway);
        timeoutGateway.subscribe(newSource);
        newSource.subscribe(failureGateway);
        failureGateway.subscribe(sink);

        return new InMemoryCompletes<>(scheduler, source, failureGateway, sink);
    }

    @Override
    public Completes<T> andThenConsume(T failedOutcomeValue, Consumer<T> consumer) {
        return andThenConsume(DEFAULT_TIMEOUT, failedOutcomeValue, consumer);
    }

    @Override
    public Completes<T> andThenConsume(long timeout, Consumer<T> consumer) {
        return andThenConsume(timeout, null, consumer);
    }

    @Override
    public Completes<T> andThenConsume(Consumer<T> consumer) {
        return andThenConsume(DEFAULT_TIMEOUT, null, consumer);
    }

    @Override
    public <F, O> O andThenTo(long timeout, F failedOutcomeValue, Function<T, O> function) {
        FailureGateway<O> failureGateway = new FailureGateway<>((O) failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, O> newSource = new AndThenToSource<>(function.andThen(e -> (Completes<O>) e).andThen(InMemorySource::fromCompletes));

        currentOperation.subscribe(timeoutGateway);
        timeoutGateway.subscribe(newSource);
        newSource.subscribe(failureGateway);
        failureGateway.subscribe((InMemorySink<O>) sink);

        return (O) new InMemoryCompletes<>(scheduler, source, failureGateway, (InMemorySink<O>) sink);
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
    public Completes<T> otherwise(Function<T, T> function) {
        Operation<T, T> otherwise = new Otherwise<>(function);
        currentOperation.subscribe(otherwise);
        otherwise.subscribe(sink);

        return new InMemoryCompletes<>(scheduler, source, otherwise, sink);
    }

    @Override
    public Completes<T> otherwiseConsume(Consumer<T> consumer) {
        Operation<T, T> otherwise = new OtherwiseConsume<>(consumer);
        currentOperation.subscribe(otherwise);
        otherwise.subscribe(sink);

        return new InMemoryCompletes<>(scheduler, source, otherwise, sink);
    }

    @Override
    public Completes<T> recoverFrom(Function<Exception, T> function) {
        Operation<T, T> newSource = new Recover<>(function);
        currentOperation.subscribe(newSource);
        newSource.subscribe(sink);

        return new InMemoryCompletes<>(scheduler, source, newSource, sink);
    }

    @Override
    public <O> O await() {
        return await(DEFAULT_TIMEOUT);
    }

    @Override
    public <O> O await(long timeout) {
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
        try {
            return sink.await(DEFAULT_TIMEOUT).get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Completes<T> repeat() {
        return this;
    }

    @Override
    public <O> Completes<O> with(O outcome) {
        source.emitOutcome(outcome);
        source.activate();

        return (Completes<O>) this;
    }
}
