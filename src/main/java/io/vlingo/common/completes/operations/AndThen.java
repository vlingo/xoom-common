// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;

import java.util.function.Function;

public class AndThen<Receives, Exposes> extends Operation<Receives, Exposes> {
    private final Function<Receives, Exposes> mapper;

    public AndThen(Function<Receives, Exposes> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(Receives receives) {
        try {
            Exposes outcome = mapper.apply(receives);
            emitOutcome(outcome);
        } catch (Exception ex) {
            emitError(ex);
        }
    }
}
