// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.version;

public class SemanticVersion {
  public static final int MAJOR_MASK = 0x7fff0000;
  public static final int MAJOR_SHIFT = 16;
  public static final int MAJOR_MAX = 32767;
  public static final int MINOR_MASK = 0x0000ff00;
  public static final int MINOR_SHIFT = 8;
  public static final int MINOR_MAX = 255;
  public static final int PATCH_MASK = 0x000000ff;
  public static final int PATCH_MAX = 255;

  public static String toString(final int version) {
    return "" + (version >> MAJOR_SHIFT) + "." + ((version & MINOR_MASK) >> MINOR_SHIFT) + "." + (version & PATCH_MASK);
  }

  public static int toValue(final int major, final int minor, final int patch) {
    if (major < 0 || major > MAJOR_MAX) {
      throw new IllegalArgumentException("Major version must be 0 to " + MAJOR_MAX);
    }
    if (minor < 0 || minor > MINOR_MAX) {
      throw new IllegalArgumentException("Minor version must 0 to " + MINOR_MAX);
    }
    if (patch < 0 || patch > PATCH_MAX) {
      throw new IllegalArgumentException("Patch version must be 0 to " + PATCH_MAX);
    }
    return ((major << MAJOR_SHIFT) & MAJOR_MASK) | ((minor << MINOR_SHIFT) & MINOR_MASK) | (patch & PATCH_MASK);
  }
}
