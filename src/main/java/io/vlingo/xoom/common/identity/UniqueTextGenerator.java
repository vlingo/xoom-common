// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.identity;

import java.util.Random;

/**
 * Generates unique text. The longer the generated String the more likely
 * it is to be unique. In tests, a length-10 String is consistently unique
 * to one million instances and greater (actually 100 million but requires
 * several seconds). When using length-7, -8, and -9, the uniqueness fails
 * before one million. Thus, the default length is 10. If you intend to use
 * this generator for short, unique identities, you should plan to ensure
 * uniqueness. Although the likelihood of uniqueness is high, you should
 * still compare against previously generated values, and if non-unique,
 * throw it out and retry generation. The uniqueness has not been tested
 * across simultaneously running JVMs.
 */
public class UniqueTextGenerator {
  private static final int DEFAULT_LENGTH = 10;

  private static final String DIGITS = "0123456789";
  private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String SYMBOLS = "!?$%^&*_-+=:;@~#|,.";

  public String generate() {
    return generate(DEFAULT_LENGTH);
  }

  public String generate(final int length) {
    return generate(length, false);
  }

  public String generate(final int length, final boolean useSymbols) {
    final StringBuffer generated = new StringBuffer();
    final Random random = new Random();
    final int maxOptions = useSymbols ? 4:3;

    while (generated.length() < length) {

        final int option = random.nextInt(maxOptions);

        final int index;

        switch (option) {
        case 0:
            index = random.nextInt(LETTERS.length());
            generated.append(LETTERS.substring(index, index+1));
            break;
        case 1:
            index = random.nextInt(LETTERS.length());
            generated.append(LETTERS.substring(index, index+1).toLowerCase());
            break;
        case 2:
            index = random.nextInt(DIGITS.length());
            generated.append(DIGITS.substring(index, index+1));
            break;
        case 3:
            index = random.nextInt(SYMBOLS.length());
            generated.append(SYMBOLS.substring(index, index+1));
            break;
        }
    }

    return generated.toString();
  }
}
