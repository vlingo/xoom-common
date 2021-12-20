package io.vlingo.xoom.common.serialization;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public interface JsonSerializationEngine {
    <T> T deserialized(String serialization, Class<T> type);

    <T> T deserialized(String serialization, Type type);

    <T> List<T> deserializedList(String serialization, Type listOfType);

    String serialized(Object instance);

    <T> String serialized(Collection<T> instance);

    <T> String serialized(List<T> instance);
}
