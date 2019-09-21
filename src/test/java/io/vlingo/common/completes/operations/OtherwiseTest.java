package io.vlingo.common.completes.operations;

import org.junit.Test;

public class OtherwiseTest extends OperationTest {
    @Test
    public void shouldRecoverFromAnError() {
        verifier().outcomeIs(4);

        source().emitError(new NullPointerException());
        source().emitCompletion();

        Otherwise<Integer> operation = new Otherwise<>(cause -> 4);
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldFailIfTheErrorRecoveryThrowsAnExceptionAndAddSourceExceptionAsSuppressed() {
        verifier().failedWith(p -> p.getSuppressed()[0].getClass() == IllegalArgumentException.class);

        source().emitError(new IllegalArgumentException());
        source().emitCompletion();

        Otherwise<Integer> operation = new Otherwise<>(cause -> { throw new NullPointerException(); });
        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
