package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.test.TestSource;
import org.junit.Test;

public class AndThenToSourceTest extends OperationTest {
    @Test
    public void shouldProcessTheOutcomeButNotChangeIt() {
        verifier().outcomeIs(2);
        verifier().outcomeIs(4);

        source().emitOutcome(2);
        source().emitCompletion();

        AndThenToSource<Integer, Integer> operation = new AndThenToSource<>(v -> {
            TestSource<Integer> multiplier = new TestSource<>();
            multiplier.emitOutcome(v);
            multiplier.emitOutcome(v * 2);
            multiplier.emitCompletion();
            multiplier.flush();

            return multiplier;
        });

        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }

    @Test
    public void shouldEmitAnyErrorReceivedOnTheSourceStream() {
        verifier().outcomeIs(2);
        verifier().outcomeIs(4);
        verifier().failedWith(NullPointerException.class);

        source().emitOutcome(2);
        source().emitOutcome(null);
        source().emitCompletion();

        AndThenToSource<Integer, Integer> operation = new AndThenToSource<>(v -> {
            TestSource<Integer> multiplier = new TestSource<>();
            multiplier.emitOutcome(v);
            multiplier.emitOutcome(v * 2);
            multiplier.emitCompletion();
            multiplier.flush();

            return multiplier;
        });

        operation.subscribe(verifier().asSink());

        source().subscribe(operation);
        source().flush();
    }
}
