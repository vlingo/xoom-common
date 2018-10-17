package io.vlingo.common.fn.outcome;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Outcome<Failure extends Throwable, Success> {
    <NextSuccess>
    Outcome<Failure, NextSuccess> andThen(final Function<Success, NextSuccess> action);

    <NextFailure extends Throwable, NextSuccess>
    Outcome<NextFailure, NextSuccess> andThenInto(final Function<Success, Outcome<NextFailure, NextSuccess>> action);

    void atLeastConsume(final Consumer<Success> consumer);

    Outcome<Failure, Success> otherwise(final Function<Failure, Success> action);

    <NextFailure extends Throwable, NextSuccess>
    Outcome<NextFailure, NextSuccess> otherwiseInto(final Function<Failure, Outcome<NextFailure, NextSuccess>> action);

    Success get() throws Failure;

    <NextSuccess>
    NextSuccess resolve(
            final Function<Failure, NextSuccess> onFailedOutcome,
            final Function<Success, NextSuccess> onSuccessfulOutcome
    );
}
