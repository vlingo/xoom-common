// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;
import io.vlingo.common.completes.Sink;
import io.vlingo.common.completes.Source;

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
            });
        } catch (Exception ex) {
            emitError(ex);
        }
    }
}
