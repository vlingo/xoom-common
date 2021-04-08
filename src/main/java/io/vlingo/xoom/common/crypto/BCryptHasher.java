// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.crypto;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptHasher implements Hasher {
  
  public BCryptHasher() { }

  @Override
  public String hash(final String plainSecret) {
    final String hashed = BCrypt.hashpw(plainSecret, BCrypt.gensalt(12));
    return hashed;
  }

  @Override
  public boolean verify(final String plainSecret, final String hashedSecret) {
    return BCrypt.checkpw(plainSecret, hashedSecret);
  }
}
