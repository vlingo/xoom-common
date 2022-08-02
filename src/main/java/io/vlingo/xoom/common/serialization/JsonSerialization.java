// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.serialization;

import io.vlingo.xoom.common.serialization.gson.GsonJsonSerialization;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class JsonSerialization {
  private static JsonSerializationStrategy strategy = new GsonJsonSerialization();

  public static <T> T deserialized(final String serialization, final Class<T> type) {
    return strategy.deserialized(serialization, type);
  }

  public static <T> T deserialized(final String serialization, final Type type) {
    return strategy.deserialized(serialization, type);
  }

  public static <T> List<T> deserializedList(final String serialization, final Type listOfType) {
    return strategy.deserializedList(serialization, listOfType);
  }

  public static String serialized(final Object instance) {
    return strategy.serialized(instance);
  }

  public static <T> String serialized(final Collection<T> instance) {
    return strategy.serialized(instance);
  }

  public static <T> String serialized(final List<T> instance) {
    return strategy.serialized(instance);
  }

  public static void use(final JsonSerializationStrategy serializationStrategy) {
    strategy = serializationStrategy;
  }
}
