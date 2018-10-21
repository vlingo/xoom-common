// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Success<CauseT extends RuntimeException, ValueT> implements Outcome<CauseT, ValueT> {
    private final ValueT value;

    private Success(final ValueT value) {
        this.value = value;
    }

    public static <CauseT extends RuntimeException, ValueT> Outcome<CauseT, ValueT> of(final ValueT value) {
        return new Success<>(value);
    }

    @Override
    public <NextSuccessT> Outcome<CauseT, NextSuccessT> andThen(final Function<ValueT, NextSuccessT> action) {
        return Success.of(action.apply(value));
    }

    @Override
    public <NextCauseT extends Throwable, NextSuccessT> Outcome<NextCauseT, NextSuccessT> andThenInto(final Function<ValueT, Outcome<NextCauseT, NextSuccessT>> action) {
        return action.apply(value);
    }

    @Override
    public void atLeastConsume(final Consumer<ValueT> consumer) {
        consumer.accept(value);
    }

    @Override
    public Outcome<CauseT, ValueT> otherwise(final Function<CauseT, ValueT> action) {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <NextCauseT extends Throwable, NextSuccessT> Outcome<NextCauseT, NextSuccessT> otherwiseInto(final Function<CauseT, Outcome<NextCauseT, NextSuccessT>> action) {
        return (Outcome<NextCauseT, NextSuccessT>) this;
    }

    @Override
    public ValueT get() throws CauseT {
        return value;
    }

    @Override
    public ValueT getOrNull() {
        return value;
    }

    @Override
    public <NextSuccessT> NextSuccessT resolve(final Function<CauseT, NextSuccessT> onFailedOutcome, final Function<ValueT, NextSuccessT> onSuccessfulOutcome) {
        return onSuccessfulOutcome.apply(value);
    }

    @Override
    public Optional<ValueT> asOptional() {
        return Optional.of(value);
    }

    @Override
    public Completes<ValueT> asCompletes() {
        return Completes.withSuccess(value);
    }

    @Override
    public Outcome<NoSuchElementException, ValueT> filter(Function<ValueT, Boolean> filterFunction) {
        if (filterFunction.apply(value)) {
            return Success.of(value);
        }

        return Failure.of(new NoSuchElementException(value.toString()));
    }

    @Override
    public <SecondSuccessT> Outcome<CauseT, Tuple2<ValueT, SecondSuccessT>> alongWith(Outcome<?, SecondSuccessT> outcome) {
        return outcome.andThenInto(secondOutcome -> Success.of(Tuple2.from(value, secondOutcome)));
    }
}
