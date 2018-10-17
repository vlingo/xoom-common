package io.vlingo.common.fn.outcome;

import java.util.function.Consumer;
import java.util.function.Function;

public class Success<Failure extends RuntimeException, Value> implements Outcome<Failure, Value> {
    private final Value value;

    private Success(Value value) {
        this.value = value;
    }

    public static <Failure extends RuntimeException, Value> Outcome<Failure, Value> of(Value value) {
        return new Success<>(value);
    }

    @Override
    public <NextSuccess> Outcome<Failure, NextSuccess> andThen(Function<Value, NextSuccess> action) {
        return Success.of(action.apply(value));
    }

    @Override
    public <NextFailure extends Throwable, NextSuccess> Outcome<NextFailure, NextSuccess> andThenInto(Function<Value, Outcome<NextFailure, NextSuccess>> action) {
        return action.apply(value);
    }

    @Override
    public void atLeastConsume(Consumer<Value> consumer) {
        consumer.accept(value);
    }

    @Override
    public Outcome<Failure, Value> otherwise(Function<Failure, Value> action) {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <NextFailure extends Throwable, NextSuccess> Outcome<NextFailure, NextSuccess> otherwiseInto(Function<Failure, Outcome<NextFailure, NextSuccess>> action) {
        return (Outcome<NextFailure, NextSuccess>) this;
    }

    @Override
    public Value get() throws Failure {
        return value;
    }

    @Override
    public Value getOrNull() {
        return value;
    }

    @Override
    public <NextSuccess> NextSuccess resolve(Function<Failure, NextSuccess> onFailedOutcome, Function<Value, NextSuccess> onSuccessfulOutcome) {
        return onSuccessfulOutcome.apply(value);
    }
}
