// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the outcome of a process that can fail in an expected way. Outcomes can be
 * mapped and composed safely, converted to Java standard Optional, to null or to a vlingo Completes.
 *
 * @param <FailureT> Type of expected error.
 * @param <SuccessT> Type of expected value.
 */
public interface Outcome<FailureT extends Throwable, SuccessT> {
    /**
     * Maps a success value to the next success value.
     *
     * For example:
     *
     * <code>
     *     Success.of(42).andThen(v -&gt; v + 8).get() // 50
     * </code>
     *
     * In case that the outcome is failure, nothing will happen.
     *
     * @param action Function to apply to the current value
     * @param <NextSuccessT> Result of the current value
     * @return A successful Outcome with the new value, or a Failure outcome.
     */
    <NextSuccessT>
    Outcome<FailureT, NextSuccessT> andThen(final Function<SuccessT, NextSuccessT> action);

    /**
     * Maps a success Outcome value to a function that returns a new Outcome, that can be
     * either successful or failure.
     *
     * @param action Function to apply to the current value
     * @param <NextFailureT> the type of the outcome on failure
     * @param <NextSuccessT> the type of the outcome on success
     * @return The mapped Outcome
     */
    <NextFailureT extends Throwable, NextSuccessT>
    Outcome<NextFailureT, NextSuccessT> andThenTo(final Function<SuccessT, Outcome<NextFailureT, NextSuccessT>> action);

    /**
     * Consumes eventually the successful outcome.
     * @param consumer A consumer function that processes the outcome.
     */
    void atLeastConsume(final Consumer<SuccessT> consumer);

    /**
     * Maps a failure outcome to a successful outcome, for recovery.
     *
     * @param action function to apply to the current value
     * @return A successful outcome.
     */
    Outcome<FailureT, SuccessT> otherwise(final Function<FailureT, SuccessT> action);

    /**
     * Maps a failure outcome to a new outcome.
     *
     * @param action function to apply to current value
     * @param <NextFailureT> the type of the outcome on failure
     * @param <NextSuccessT> the type of the outcome on success
     * @return The mapped outcome.
     */
    <NextFailureT extends Throwable, NextSuccessT>
    Outcome<NextFailureT, NextSuccessT> otherwiseTo(final Function<FailureT, Outcome<NextFailureT, NextSuccessT>> action);

    /**
     * @return The success outcome value
     * @throws FailureT in case that the outcome is a failure
     */
    SuccessT get() throws FailureT;

    /**
     *
     * @return The success outcome value or null in case of a failure
     */
    SuccessT getOrNull();

    /**
     * Resolves the outcome and returns the mapped value.
     *
     * For example:
     *
     * <code>
     *     Failure.of(exception).resolve(f -&gt; 42, s -@gtl  1) // == 42
     *     Success.of(value).resolve(f -@gt; 42, s -@gt;  1) // == 1
     * </code>
     *
     * @param onFailedOutcome A mapping function from a failure to a success outcome
     * @param onSuccessfulOutcome A mapping function from a success outcome to another success outcome
     * @param <NextSuccessT> the type of the next success
     * @return The mapped value
     */
    <NextSuccessT>
    NextSuccessT resolve(
            final Function<FailureT, NextSuccessT> onFailedOutcome,
            final Function<SuccessT, NextSuccessT> onSuccessfulOutcome
    );

    /**
     *
     * @return A Java optional with the success value, or Optional.empty() in case of failure
     */
    Optional<SuccessT> asOptional();

    /**
     *
     * @return A vlingo Completes with the success value, or a failed Completes n case of failure
     */
    Completes<SuccessT> asCompletes();

    /**
     * Applies a filter predicate to the success value, or returns a failed Outcome in case
     * of not fulfilling the predicate.
     *
     * @param filterFunction the filter function
     * @return The filtered outcome
     */
    Outcome<NoSuchElementException, SuccessT> filter(final Function<SuccessT, Boolean> filterFunction);

    /**
     * Returns a Outcome of a tuple of successes, or the first Failure in case of any of the failed outcomes.
     *
     * @param outcome the outcome
     * @param <SecondSuccessT> the type of the second success
     * @return a Outcome of a tuple of successes, or the first Failure in case of any of the failed outcomes
     */
    <SecondSuccessT>
    Outcome<FailureT, Tuple2<SuccessT, SecondSuccessT>> alongWith(final Outcome<?, SecondSuccessT> outcome);

    /**
     * Maps a failed outcome to another failed outcome.
     *
     * @param action the function to map a failed outcome to another failed outcome
     * @param <NextFailureT> the type of the next failure
     * @return The new failed outcome
     */
    <NextFailureT extends Throwable>
    Outcome<NextFailureT, SuccessT> otherwiseFail(Function<FailureT, NextFailureT> action);
}
