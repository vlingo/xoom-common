// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.crypto;

import java.util.Properties;

public interface Hasher {
  static Hasher defaultHasher(final Properties properties) {
    switch (properties.getProperty("crypto.type", "(unknown)")) {
    case "argon2":
      final int cryptoArgon2MaxDuration = Integer.parseInt(properties.getProperty("crypto.argon2.max.duration", "10"));
      final int cryptoArgon2MemoryCost = Integer.parseInt(properties.getProperty("crypto.argon2.memory.cost", "65536"));
      final int cryptoArgon2Parallelism = Integer.parseInt(properties.getProperty("crypto.argon2.parallelism", "1"));
      return new Argon2Hasher(cryptoArgon2MaxDuration, cryptoArgon2MemoryCost, cryptoArgon2Parallelism);
    case "scrypt":
      final int cryptoScrypt_N_costFactor = Integer.parseInt(properties.getProperty("crypto.scrypt.N.cost.factor", "16384"));
      final int cryptoScrypt_r_Blocksize = Integer.parseInt(properties.getProperty("crypto.scrypt.r.blocksize", "8"));
      final int cryptoScrypt_p_parallelization = Integer.parseInt(properties.getProperty("crypto.scrypt.p.parallelization", "1"));
      return new SCryptHasher(cryptoScrypt_N_costFactor, cryptoScrypt_r_Blocksize, cryptoScrypt_p_parallelization);
    case "bcrypt":
      return new BCryptHasher();
    }
    throw new IllegalStateException("Crypto type is not defined.");
  }

  String hash(final String plainSecret);
  boolean verify(final String plainSecret, final String hashedSecret);
}
