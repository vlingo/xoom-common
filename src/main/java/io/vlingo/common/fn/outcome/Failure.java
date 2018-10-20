package io.vlingo.common.fn.outcome;

import java.util.function.Consumer;
import java.util.function.Function;

public class Failure<Cause extends RuntimeException, Value> implements Outcome<Cause, Value> {
    private final Cause cause;

    private Failure(Cause cause) {
        this.cause = cause;
    }

    public static <Cause extends RuntimeException, Value> Outcome<Cause, Value> of(Cause cause) {
        return new Failure<>(cause);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <NextSuccess> Outcome<Cause, NextSuccess> andThen(Function<Value, NextSuccess> action) {
        return (Outcome<Cause, NextSuccess>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <NextFailure extends Throwable, NextSuccess> Outcome<NextFailure, NextSuccess> andThenInto(Function<Value, Outcome<NextFailure, NextSuccess>> action) {
        return (Outcome<NextFailure, NextSuccess>) this;
    }

    @Override
    public void atLeastConsume(Consumer<Value> consumer) {

    }

    @Override
    public Outcome<Cause, Value> otherwise(Function<Cause, Value> action) {
        return Success.of(action.apply(cause));
    }

    @Override
    public <NextFailure extends Throwable, NextSuccess> Outcome<NextFailure, NextSuccess> otherwiseInto(Function<Cause, Outcome<NextFailure, NextSuccess>> action) {
        return action.apply(cause);
    }

    @Override
    public Value get() throws Cause {
        throw cause;
    }

    @Override
    public Value getOrNull() {
        return null;
    }

    @Override
    public <NextSuccess> NextSuccess resolve(Function<Cause, NextSuccess> onFailedOutcome, Function<Value, NextSuccess> onSuccessfulOutcome) {
        return onFailedOutcome.apply(cause);
    }
}
