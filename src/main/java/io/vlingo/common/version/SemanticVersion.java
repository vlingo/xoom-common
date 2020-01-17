// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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

  public static SemanticVersion from(final int major, final int minor, final int patch) {
    return new SemanticVersion(major, minor, patch);
  }

  public static SemanticVersion from(final String version) {
    final String[] parts = version.split("\\.");
    if (parts.length == 3) {
      final int major = Integer.parseInt(parts[0]);
      final int minor = Integer.parseInt(parts[1]);
      final int patch = Integer.parseInt(parts[2]);
      return new SemanticVersion(major, minor, patch);
    } else {
      return new SemanticVersion(0, 0, 0);
    }
  }

  public static SemanticVersion greatest() {
    return from(MAJOR_MAX, MINOR_MAX, PATCH_MAX);
  }

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

  public static int toValue(final String version) {
    final String[] parts = version.split("\\.");
    if (parts.length == 3) {
      return toValue(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    } else {
      return zero();
    }
  }

  public static int zero() {
    return 0;
  }

  public final int major;
  public final int minor;
  public final int patch;

  public boolean isCompatibleWith(final SemanticVersion previous) {
    if (major == previous.major && minor == previous.minor && patch == previous.patch + 1) {
      return true;
    }
    if (major == previous.major && minor == previous.minor + 1 && patch == previous.patch) {
      return true;
    }
    if (major == previous.major + 1 && minor == previous.minor && patch == previous.patch) {
      return true;
    }
    return false;
  }

  public boolean isGreaterThan(final SemanticVersion version) {
    if (major > version.major) {
      return true;
    }
    if (major == version.major && minor > version.minor) {
      return true;
    }
    if (major == version.major && minor == version.minor && patch > version.patch) {
      return true;
    }
    return false;
  }

  public boolean isNonZero() {
    return !(major == 0 && minor == 0 && patch == 0);
  }

  public SemanticVersion withIncrementedMajor() {
    return new SemanticVersion(major + 1, minor, patch);
  }

  public SemanticVersion withIncrementedMinor() {
    return new SemanticVersion(major, minor + 1, patch);
  }

  public SemanticVersion withIncrementedPatch() { return new SemanticVersion(major, minor, patch + 1); }

  public SemanticVersion nextPatch() { return withIncrementedPatch(); }

  public SemanticVersion nextMinor() { return new SemanticVersion(major, minor + 1, 0); }

  public SemanticVersion nextMajor() { return new SemanticVersion(major + 1, 0, 0); }

  @Override
  public int hashCode() {
    return 31 * (major + minor + patch + 1);
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || other.getClass() != getClass()) {
      return false;
    }

    final SemanticVersion otherVersion = (SemanticVersion) other;

    return this.major == otherVersion.major && this.minor == otherVersion.minor && this.patch == otherVersion.patch;
  }

  @Override
  public String toString() {
    return "" + major + "." + minor + "." + patch;
  }

  public int toValue() {
    return toValue(major, minor, patch);
  }

  private SemanticVersion(final int major, final int minor, final int patch) {
    assert (major >= 0 && major <= MAJOR_MAX);
    this.major = major;
    assert (minor >= 0 && minor <= MINOR_MAX);
    this.minor = minor;
    assert (patch >= 0 && patch <= PATCH_MAX);
    this.patch = patch;
  }
}
