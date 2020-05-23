// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.operations.AndThen;
import io.vlingo.common.completes.operations.AndThenConsume;
import io.vlingo.common.completes.operations.AndThenToSource;
import io.vlingo.common.completes.operations.FailureGateway;
import io.vlingo.common.completes.operations.Otherwise;
import io.vlingo.common.completes.operations.OtherwiseConsume;
import io.vlingo.common.completes.operations.Recover;
import io.vlingo.common.completes.operations.TimeoutGateway;
import io.vlingo.common.completes.sinks.InMemorySink;
import io.vlingo.common.completes.sources.InMemorySource;

public class SinkAndSourceBasedCompletes<T> implements Completes<T> {
    private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;

    private final Scheduler scheduler;
    public final Source<Object> source;
    private final Source<T> currentOperation;
    private final Sink<T> sink;

    protected SinkAndSourceBasedCompletes(Scheduler scheduler, Source<Object> source, Source<T> currentOperation, Sink<T> sink) {
        this.scheduler = scheduler;
        this.source = source;
        this.sink = sink;
        this.currentOperation = currentOperation;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected SinkAndSourceBasedCompletes(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.source = new InMemorySource<>();
        this.sink = new InMemorySink<>();
        this.currentOperation = (Source<T>) source;

        source.subscribe((Sink) sink);
    }

    @SuppressWarnings("unchecked")
    public static <T> SinkAndSourceBasedCompletes<T> withScheduler(Scheduler scheduler) {
        InMemorySource<T> source = new InMemorySource<>();
        InMemorySink<T> sink = new InMemorySink<>();

        source.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, (Source<Object>) source, source, sink);
    }

    public static boolean isToggleActive() {
        return Boolean.parseBoolean(System.getProperty("vlingo.InMemoryCompletes", "false"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> andThen(long timeout, O failedOutcomeValue, Function<T, O> function) {
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

        return new SinkAndSourceBasedCompletes<T>(scheduler, source, failureGateway, sink);
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
    @SuppressWarnings("unchecked")
    public <F, O> O andThenTo(long timeout, F failedOutcomeValue, Function<T, O> function) {
        FailureGateway<O> failureGateway = new FailureGateway<>((O) failedOutcomeValue);
        TimeoutGateway<T> timeoutGateway = new TimeoutGateway<>(scheduler, timeout);
        Operation<T, O> newSource = new AndThenToSource<>(function.andThen(e -> (Completes<O>) e).andThen(InMemorySource::fromCompletes));

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <E> Completes<T> otherwise(Function<E, T> function) {
        Operation<T, T> otherwise = new Otherwise(function);
        currentOperation.subscribe(otherwise);
        otherwise.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, otherwise, sink);
    }

    @Override
    public Completes<T> otherwiseConsume(Consumer<T> consumer) {
        Operation<T, T> otherwise = new OtherwiseConsume<>(consumer);
        currentOperation.subscribe(otherwise);
        otherwise.subscribe(sink);

        return new SinkAndSourceBasedCompletes<>(scheduler, source, otherwise, sink);
    }

    @Override
    public Completes<T> recoverFrom(Function<Exception, T> function) {
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
    @SuppressWarnings("unchecked")
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
    public void failed(final Exception exception) {
      source.emitError(exception);
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
    public Completes<T> timeoutWithin(final long timeout) {
      // TODO: Implement
      return this;
    }

    @Override
    public <F> Completes<T> useFailedOutcomeOf(final F failedOutcomeValue) {
      // TODO: Implement
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> andFinally() {
      return andFinally(value -> (O) value);
    }

    @Override
    public <O> Completes<O> andFinally(final Function<T,O> function) {
        final Completes<O> edge = andThen(function);
        source.activate();
        return edge;
    }

    @Override
    public void andFinallyConsume(Consumer<T> consumer) {
        andThenConsume(consumer);
        source.activate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(O outcome) {
        source.emitOutcome(outcome);
        return (SinkAndSourceBasedCompletes<O>) this;
    }

    @Override
    public String toString() {
        return "SinkAndSourceBasedCompletes{" +
                "scheduler=" + scheduler +
                ", source=" + source +
                ", currentOperation=" + currentOperation +
                ", sink=" + sink +
                '}';
    }
}
