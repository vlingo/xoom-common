package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.test.SinkVerifier;
import io.vlingo.common.completes.test.TestSink;
import io.vlingo.common.completes.test.TestSource;
import org.junit.Before;

public abstract class OperationTest {
    private SinkVerifier<Integer> verifier;
    private TestSource<Integer> source;

    @Before
    public final void setUp() {
        verifier = new TestSink<>();
        source = new TestSource<>();
    }

    protected final SinkVerifier<Integer> verifier() {
        return verifier;
    }

    protected final TestSource<Integer> source() {
        return source;
    }
}
