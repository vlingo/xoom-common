package io.vlingo.common.completes;

import io.vlingo.common.Completes;
import io.vlingo.common.completes.operations.AndThen;
import io.vlingo.common.completes.operations.AndThenConsume;
import io.vlingo.common.completes.operations.Recover;
import io.vlingo.common.completes.sinks.InMemorySink;
import io.vlingo.common.completes.sources.InMemorySource;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class InMemoryCompletes<T> implements Completes<T> {
    private final InMemorySource<Object> source;
    private final Source<T> currentOperation;
    private final InMemorySink<T> sink;

    private InMemoryCompletes(InMemorySource<Object> source, Source<T> currentOperation, InMemorySink<T> sink) {
        this.source = source;
        this.sink = sink;
        this.currentOperation = currentOperation;
    }

    public static boolean isToggleActive() {
        return Boolean.parseBoolean(System.getProperty("vlingo.InMemoryCompletes", "false"));
    }

    @Override
    public <O> Completes<O> andThen(long timeout, O failedOutcomeValue, Function<T, O> function) {
        Operation<T, O> newSource = new AndThen<>(function);
        currentOperation.subscribe(newSource);
        return new InMemoryCompletes<>(source, newSource, (InMemorySink<O>) sink);
    }

    @Override
    public <O> Completes<O> andThen(O failedOutcomeValue, Function<T, O> function) {
        return andThen(-1L, failedOutcomeValue, function);
    }

    @Override
    public <O> Completes<O> andThen(long timeout, Function<T, O> function) {
        return andThen(timeout, null, function);
    }

    @Override
    public <O> Completes<O> andThen(Function<T, O> function) {
        return andThen(-1L, null, function);
    }

    @Override
    public Completes<T> andThenConsume(long timeout, T failedOutcomeValue, Consumer<T> consumer) {
        Operation<T, T> newSource = new AndThenConsume<>(consumer);
        currentOperation.subscribe(newSource);
        return new InMemoryCompletes<>(source, newSource, sink);
    }

    @Override
    public Completes<T> andThenConsume(T failedOutcomeValue, Consumer<T> consumer) {
        return andThenConsume(-1L, failedOutcomeValue, consumer);
    }

    @Override
    public Completes<T> andThenConsume(long timeout, Consumer<T> consumer) {
        return andThenConsume(timeout, null, consumer);
    }

    @Override
    public Completes<T> andThenConsume(Consumer<T> consumer) {
        return andThenConsume(-1L, null, consumer);
    }

    @Override
    public <F, O> O andThenTo(long timeout, F failedOutcomeValue, Function<T, O> function) {
        return null;
    }

    @Override
    public <F, O> O andThenTo(F failedOutcomeValue, Function<T, O> function) {
        return null;
    }

    @Override
    public <O> O andThenTo(long timeout, Function<T, O> function) {
        return null;
    }

    @Override
    public <O> O andThenTo(Function<T, O> function) {
        return null;
    }

    @Override
    public Completes<T> otherwise(Function<T, T> function) {
        return null;
    }

    @Override
    public Completes<T> otherwiseConsume(Consumer<T> consumer) {
        return null;
    }

    @Override
    public Completes<T> recoverFrom(Function<Exception, T> function) {
        Operation<T, T> newSource = new Recover<>(function);
        currentOperation.subscribe(newSource);
        return new InMemoryCompletes<>(source, newSource, sink);
    }

    @Override
    public <O> O await() {
        try {
            Optional<T> value = sink.await();
            if (value.isPresent()) {
                return (O) value.get();
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return null;
    }

    @Override
    public <O> O await(long timeout) {
        try {
            Optional<T> value = sink.await(timeout);
            if (value.isPresent()) {
                return (O) value.get();
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
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
            return sink.await().get();
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
        return (Completes<O>) this;
    }
}
