// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import java.util.Optional;

public final class OutcomeConversions {
    private OutcomeConversions() {}

    public static Optional<Integer> asOptional(Outcome<RuntimeException, Integer> outcome) {
        return outcome.resolve(ex -> Optional.empty(), Optional::of);
    }
}