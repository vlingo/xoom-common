package io.vlingo.common.fn.outcome;

import java.util.Optional;

public final class OutcomeConversions {
    private OutcomeConversions() {}

    public static Optional<Integer> asOptional(Outcome<RuntimeException, Integer> outcome) {
        return outcome.resolve(ex -> Optional.empty(), Optional::of);
    }
}
