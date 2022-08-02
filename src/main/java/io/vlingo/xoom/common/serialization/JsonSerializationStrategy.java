package io.vlingo.xoom.common.serialization;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public interface JsonSerializationStrategy {
  <T> T deserialized(final String serialization, final Class<T> type);

  <T> T deserialized(final String serialization, final Type type);

  <T> List<T> deserializedList(final String serialization, final Type listOfType);

  String serialized(final Object instance);

  <T> String serialized(final Collection<T> instance);

  <T> String serialized(final List<T> instance);
}
