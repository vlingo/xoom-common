// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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

public class Failure<CauseT extends Throwable, ValueT> implements Outcome<CauseT, ValueT> {
    private final CauseT cause;

    private Failure(final CauseT cause) {
        this.cause = cause;
    }

    public static <CauseT extends Throwable, ValueT> Outcome<CauseT, ValueT> of(final CauseT cause) {
        return new Failure<>(cause);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <NextSuccessT> Outcome<CauseT, NextSuccessT> andThen(final Function<ValueT, NextSuccessT> action) {
        return (Outcome<CauseT, NextSuccessT>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <NextFailure extends Throwable, NextSuccessT> Outcome<NextFailure, NextSuccessT> andThenTo(final Function<ValueT, Outcome<NextFailure, NextSuccessT>> action) {
        return (Outcome<NextFailure, NextSuccessT>) this;
    }

    @Override
    public void atLeastConsume(final Consumer<ValueT> consumer) {

    }

    @Override
    public Outcome<CauseT, ValueT> otherwise(final Function<CauseT, ValueT> action) {
        return Success.of(action.apply(cause));
    }

    @Override
    public <NextFailure extends Throwable, NextSuccessT> Outcome<NextFailure, NextSuccessT> otherwiseTo(final Function<CauseT, Outcome<NextFailure, NextSuccessT>> action) {
        return action.apply(cause);
    }

    @Override
    public ValueT get() throws CauseT {
        throw cause;
    }

    @Override
    public ValueT getOrNull() {
        return null;
    }

    @Override
    public <NextSuccessT> NextSuccessT resolve(final Function<CauseT, NextSuccessT> onFailedOutcome, final Function<ValueT, NextSuccessT> onSuccessfulOutcome) {
        return onFailedOutcome.apply(cause);
    }

    @Override
    public Optional<ValueT> asOptional() {
        return Optional.empty();
    }

    @Override
    public Completes<ValueT> asCompletes() {
        return Completes.withFailure();
    }

    @Override
    public Outcome<NoSuchElementException, ValueT> filter(Function<ValueT, Boolean> filterFunction) {
        return Failure.of((NoSuchElementException) new NoSuchElementException().initCause(cause));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <SecondSuccessT> Outcome<CauseT, Tuple2<ValueT, SecondSuccessT>> alongWith(Outcome<?, SecondSuccessT> outcome) {
        return (Outcome<CauseT, Tuple2<ValueT, SecondSuccessT>>) this;
    }

    @Override
    public <NextFailureT extends Throwable> Outcome<NextFailureT, ValueT> otherwiseFail(Function<CauseT, NextFailureT> action) {
        return Failure.of(action.apply(cause));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
      if (other == null || !other.getClass().equals(getClass())) {
        return false;
      }
      return this.cause.equals(((Failure<CauseT, ValueT>) other).cause);
    }

    @Override
    public int hashCode() {
      return 31 * cause.hashCode();
    }
}
