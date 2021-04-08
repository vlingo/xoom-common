// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class UniqueTextGeneratorTest {

  @Test
  public void testThatUniqueTextGenerates() {
    final int length = 10;
    final int cycles = 100;
    final int maximum = 10_000;
    final int total = cycles * maximum;

    final Set<String> all = new HashSet<>(total);

    final UniqueTextGenerator generator = new UniqueTextGenerator();

    for (int count = 0; count < cycles; ++count) {
      for (int idx = 0; idx < maximum; ++idx) {
        final String generated = generator.generate(length);
        assertEquals(length, generated.length());
        //System.out.println(generated);
        assertTrue(all.add(generated));
      }
    }

    assertEquals(total, all.size());
  }
}
