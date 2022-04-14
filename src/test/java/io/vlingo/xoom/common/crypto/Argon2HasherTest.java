// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.crypto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Argon2HasherTest {

  @Test
  public void testThatHashVerifiesSimple() {
    final String secret = "secret";
    final Argon2Hasher hasher = new Argon2Hasher(2, 65535, 1);
    final String hashed = hasher.hash(secret);
    assertTrue(hasher.verify(secret, hashed));
  }

  @Test
  public void testThatHashVerifiesComplext() {
    final String secret = "Thi$ isAM0re C*mple+ S3CR37";
    final Argon2Hasher hasher = new Argon2Hasher(10, 65535, 1);
    final String hashed = hasher.hash(secret);
    assertTrue(hasher.verify(secret, hashed));
  }

  @Test
  public void testThatHashVerifiesComplextGreaterTiming() {
    final String secret = "(Thi$) isAn Ev0nM0re c*mple+ S3CR37 --!.";
    final Argon2Hasher hasher = new Argon2Hasher(20, 65535, 1);
    final String hashed = hasher.hash(secret);
    assertTrue(hasher.verify(secret, hashed));
  }
}
