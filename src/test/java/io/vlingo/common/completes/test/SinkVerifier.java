// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.test;

import io.vlingo.common.completes.Sink;

import java.util.function.Predicate;

public interface SinkVerifier<Outcome> {
    SinkVerifier<Outcome> outcomeIs(Outcome outcome);
    SinkVerifier<Outcome> outcomeIs(Predicate<Outcome> predicate);

    SinkVerifier<Outcome> failedWith(Exception ex);
    SinkVerifier<Outcome> failedWith(Class<? extends Exception> exClass);
    SinkVerifier<Outcome> failedWith(Predicate<Exception> predicate);

    Sink<Outcome> asSink();
}
