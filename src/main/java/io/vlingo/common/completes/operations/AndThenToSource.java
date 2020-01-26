// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;
import io.vlingo.common.completes.Sink;
import io.vlingo.common.completes.Source;

import java.util.Optional;
import java.util.function.Function;

public class AndThenToSource<Receives, Exposes> extends Operation<Receives, Exposes> {
    private final Function<Receives, Source<Exposes>> mapper;

    public AndThenToSource(Function<Receives, Source<Exposes>> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(Receives receives) {
        try {
            Source<Exposes> sourceToSubscribe = mapper.apply(receives);
            sourceToSubscribe.subscribe(new Sink<Exposes>() {
                @Override
                public void onOutcome(Exposes exposes) {
                    emitOutcome(exposes);
                }

                @Override
                public void onError(Exception cause) {
                    emitError(cause);
                }

                @Override
                public void onCompletion() {
                    // this completion does not trigger the parent completion
                }

                @Override
                public boolean hasBeenCompleted() {
                    return false;
                }

                @Override
                public Optional<Exposes> await(long timeout) throws Exception {
                    return Optional.empty();
                }

                @Override
                public boolean hasFailed() {
                    return false;
                }

                @Override
                public boolean hasOutcome() {
                    return false;
                }

                @Override
                public void repeat() {

                }
            });

            sourceToSubscribe.activate();
        } catch (Exception ex) {
            emitError(ex);
        }
    }
}
