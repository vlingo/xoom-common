// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.crypto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BCryptHasherTest {

  @Test
  public void testThatHashVerifiesSimple() {
    final String secret = "secret";
    final BCryptHasher hasher = new BCryptHasher();
    final String hashed = hasher.hash(secret);
    assertTrue(hasher.verify(secret, hashed));
  }

  @Test
  public void testThatHashVerifiesComplext() {
    final String secret = "Thi$ isAM0re c*mplex+ S3CR37";
    final BCryptHasher hasher = new BCryptHasher();
    final String hashed = hasher.hash(secret);
    assertTrue(hasher.verify(secret, hashed));
  }
}
