// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import java.util.Optional;

public interface Sink<Receives> {
    void onOutcome(Receives receives);
    void onError(Exception cause);
    void onCompletion();
    boolean hasBeenCompleted();
    Optional<Receives> await(long timeout) throws Exception;
    boolean hasFailed();
    boolean hasOutcome();
    void repeat();
}
