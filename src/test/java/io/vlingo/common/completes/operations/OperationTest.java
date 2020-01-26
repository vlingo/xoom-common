// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

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
