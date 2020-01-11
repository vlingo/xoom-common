// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.identity;

import io.vlingo.common.identity.IdentityGenerator.NameBasedIdentityGenerator;
import io.vlingo.common.identity.IdentityGenerator.RandomIdentityGenerator;
import io.vlingo.common.identity.IdentityGenerator.TimeBasedIdentityGenerator;

public enum IdentityGeneratorType {
  TIME_BASED { public IdentityGenerator generator() { return new TimeBasedIdentityGenerator(); } },
  NAME_BASED { public IdentityGenerator generator() { return new NameBasedIdentityGenerator(); } },
  RANDOM    { public IdentityGenerator generator() { return new RandomIdentityGenerator(); } };

  public IdentityGenerator generator() { return null; }

  public static final IdentityGenerator TimeBased = TIME_BASED.generator();
  public static final IdentityGenerator NameBased = NAME_BASED.generator();
  public static final IdentityGenerator Random = RANDOM.generator();
}
