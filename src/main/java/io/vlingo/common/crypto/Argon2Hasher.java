// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.crypto;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2Hasher implements Hasher {
  private final Argon2 argon2;
  private final int maxDuration;
  private final int memoryCost;
  private final int parallelism;

  public Argon2Hasher(final int maxDuration, final int memoryCost, final int parallelism) {
    this.argon2 = Argon2Factory.create();
    this.maxDuration = maxDuration;
    this.memoryCost = memoryCost;
    this.parallelism = parallelism;
  }

  @Override
  public String hash(final String plainSecret) {
    final String hash = argon2.hash(maxDuration, memoryCost, parallelism, plainSecret);
    return hash;
  }

  @Override
  public boolean verify(final String plainSecret, final String hashedSecret) {
    return argon2.verify(hashedSecret, plainSecret);
  }
}
