package io.vlingo.common;

import java.util.function.Function;


/**
 * Monad transformer implementation for Outcome nested in Completes
 *
 * @param <E> the Outcome's error type
 * @param <T> the Outcome's success type
 */
public final class CompletesOutcomeT<E extends Throwable, T> {

    public static <EE extends Throwable, TT> CompletesOutcomeT<EE, TT> of(
        Completes<Outcome<EE, TT>> value) {

        return new CompletesOutcomeT<>(value);
    }


    private final Completes<Outcome<E, T>> value;


    private CompletesOutcomeT(Completes<Outcome<E, T>> value) {
        this.value = value;
    }


    public final <O> CompletesOutcomeT<E, O> andThen(Function<T, O> function) {
        return new CompletesOutcomeT<>(
            value.andThen(outcome ->
                outcome.andThen(function)));
    }

    public final <O> CompletesOutcomeT<E, O> andThenTo(
        Function<T, CompletesOutcomeT<E, O>> function) {

        return new CompletesOutcomeT<>(value.andThenTo(outcome -> {
            if (outcome instanceof Failure) {
                return Completes.withSuccess(outcome.andThenTo(null));
            }
            else {
                try {
                    return function.apply(outcome.get()).value();
                } catch (Throwable t) {
                    throw new RuntimeException(
                        "Unexpected exception thrown getting the value out of a successful Outcome!", t);
                }
            }
        }));
    }

    public final Completes<Outcome<E, T>> value() {
        return value;
    }
}
