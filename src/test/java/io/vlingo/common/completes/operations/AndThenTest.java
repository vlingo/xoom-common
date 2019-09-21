package io.vlingo.common.completes.operations;

import org.junit.Test;

public class AndThenTest extends OperationTest {
    @Test
    public void shouldMapAValueAndExposeTheOutcome() {
        verifier().outcomeIs(420);

        source().emitOutcome(42);
        source().emitCompletion();

        AndThen<Integer, Integer> operation = new AndThen<>(a -> a * 10);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldPassThroughErrors() {
        Exception cause = new RuntimeException("Yay!");

        verifier().failedWith(cause);

        source().emitError(cause);
        source().emitCompletion();

        AndThen<Integer, Integer> operation = new AndThen<>(a -> a * 10);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldEmitAnErrorIfTheMapperFails() {
        verifier().failedWith(NullPointerException.class);

        source().emitOutcome(null);
        source().emitCompletion();

        AndThen<Integer, Integer> operation = new AndThen<>(a -> a * 10);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
